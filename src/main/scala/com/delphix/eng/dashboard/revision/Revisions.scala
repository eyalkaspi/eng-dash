/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.revision

import scala.slick.driver.H2Driver.simple._
import scala.slick.driver.H2Driver.simple.Database.threadLocalSession
import scala.slick.lifted.BaseTypeMapper
import com.delphix.eng.dashboard.git.commit.CommitId
import com.delphix.eng.dashboard.persistence.TypeMappers
import com.delphix.eng.dashboard.persistence.TypeMappers._
import com.google.inject.Inject
import com.delphix.eng.dashboard.persistence.RepoTable
import com.delphix.eng.dashboard.persistence.Id
import RevisionState._
import com.delphix.eng.dashboard.vm.VmIdentifier
import com.delphix.eng.dashboard.git.commit.Author
import com.delphix.eng.dashboard.git.commit.Author
import com.delphix.eng.dashboard.git.commit.Author

class Revisions @Inject() (val db: Database) {

  implicit val stateTypeMapper = MappedTypeMapper.base[RevisionState, String](
    { state => state.toString() },
    { name => RevisionState withName name })

  implicit val vmIdentifierMapper = MappedTypeMapper.base[VmIdentifier, String](
    { vm => vm.id },
    { name => new VmIdentifier(name) })

  implicit val authorMapper = MappedTypeMapper.base[Author, String](
    { author => author.email },
    { email => new Author(email) })

  val table = new RepoTable[Revision]("REVISION")(TypeMappers.revisionTypeMapper) {
    def commitId = column[CommitId]("COMMIT_ID", O.NotNull)
    def state = column[RevisionState]("STATE")
    def vm = column[VmIdentifier]("VM")
    def author = column[Author]("AUTHOR")
    def * = id.? ~ commitId ~ state ~ vm ~ author <> (Revision, Revision.unapply _)
    def uniqueCommit = index("unique_commit_id", commitId, unique = true)
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

  def save(commitId: CommitId, vm: VmIdentifier, author: Author): Id[Revision] = {
    db withSession {
      table.autoInc.insert(Revision(Option.empty, commitId, RevisionState.INITIAL, vm, author))
    }
  }

  def list(): List[Revision] = {
    db withSession {
      Query(table).list
    }
  }

}
