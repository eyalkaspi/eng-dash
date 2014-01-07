/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.revision

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.driver.PostgresDriver.simple.Database.threadLocalSession
import scala.slick.lifted.BaseTypeMapper
import com.delphix.eng.dashboard.git.commit.CommitId
import com.delphix.eng.dashboard.persistence.TypeMappers
import com.delphix.eng.dashboard.persistence.TypeMappers._
import com.google.inject.Inject
import com.delphix.eng.dashboard.persistence.RepoTable
import com.delphix.eng.dashboard.persistence.Id
import RevisionState._
import RevisionType._
import com.delphix.eng.dashboard.vm.VmIdentifier
import com.delphix.eng.dashboard.git.commit.Author

class Revisions @Inject() (val db: Database) {

  implicit val stateTypeMapper = MappedTypeMapper.base[RevisionState, String](
    { state => state.toString() },
    { name => RevisionState withName name })

  implicit val revTypeTypeMapper = MappedTypeMapper.base[RevisionType, String](
	{ state => state.toString() },
	{ name => RevisionType withName name })

  implicit val vmIdentifierMapper = MappedTypeMapper.base[VmIdentifier, String](
    { vm => vm.id },
    { name => new VmIdentifier(name) })

  implicit val authorMapper = MappedTypeMapper.base[Author, String](
    { author => author.email },
    { email => new Author(email) })

  case class NewRevision(commitId: CommitId, state: RevisionState,
    revisionType: RevisionType, vm: VmIdentifier, author: Author, commitMsg: String)

  val table = new RepoTable[Revision]("revision")(TypeMappers.revisionTypeMapper) {
    def commitId = column[CommitId]("COMMIT_ID", O.NotNull)
    def state = column[RevisionState]("STATE")
    def vm = column[VmIdentifier]("VM")
    def author = column[Author]("AUTHOR")
    def commitMsg = column[String]("COMMIT_MSG")
    def revType = column[RevisionType]("REV_TYPE")
    def * = id.? ~ commitId ~ state ~ revType ~ vm ~ author ~ commitMsg <> (Revision, Revision.unapply _)
    def autoInc = commitId ~ state ~ revType ~ vm ~ author ~ commitMsg <> (NewRevision, NewRevision.unapply _) returning id
  }

  def createDDl() = {
    db withSession {
      table.ddl.create
    }
  }

  def get(id: Id[Revision]) = {
    db withSession {
      (for (f <- table if f.id.asColumnOf[Int] === id.id) yield f) first
    }
  }

  def getByCommitId(id: CommitId) = {
    db withSession {
      (for (f <- table if f.commitId.asColumnOf[String] === id.id) yield f) firstOption
    }
  }

  def updateState(id: Id[Revision], state: RevisionState) = {
    db withSession {
      val q = for (f <- table if f.id.asColumnOf[Int] === id.id) yield f.state
      val r = q.update(state)
      require(r == 1)
    }
  }
  
  def updateMsg(id: Id[Revision], msg: String) = {
    db withSession {
      val q = for (f <- table if f.id.asColumnOf[Int] === id.id) yield f.commitMsg
      val r = q.update(msg)
      require(r == 1)
    }
  }

  def save(commitId: CommitId, vm: VmIdentifier, author: Author,
      commitMsg: String, revType: RevisionType): Id[Revision] = {
    db withSession {
      table.autoInc.insert(NewRevision(commitId,
          RevisionState.INITIAL, revType, vm, author, commitMsg))
    }
  }

  def list(): List[Revision] = {
    db withSession {
      Query(table).sortBy(_.id.desc).list()
    }
  }

}
