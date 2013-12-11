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

class Revisions @Inject() (val db: Database) {

  implicit val stateTypeMapper = MappedTypeMapper.base[RevisionState, String](
    { state => state.toString() },
    { name => RevisionState withName name })

  val table = new RepoTable[Revision]("REVISION")(TypeMappers.revisionTypeMapper) {
    def commitId = column[CommitId]("COMMIT_ID", O.NotNull)
    def state = column[RevisionState]("STATE")
    def * = id.? ~ commitId ~ state <> (Revision, Revision.unapply _)
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

  def updateState(id: Id[Revision], state: RevisionState) = {
    db withSession {
      val q = for (f <- table if f.id.asColumnOf[Int] === id.id) yield f.state
      val r = q.update(state)
      require(r == 1)
    }
  }

  def save(commitId: CommitId): Id[Revision] = {
    db withSession {
      table.autoInc.insert(Revision(Option.empty, commitId, RevisionState.INITIAL))
    }
  }

  def list(): List[Revision] = {
    db withSession {
      Query(table).list
    }
  }

}
