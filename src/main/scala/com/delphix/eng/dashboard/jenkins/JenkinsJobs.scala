/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.jenkins

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.driver.PostgresDriver.simple.Database.threadLocalSession
import com.delphix.eng.dashboard.persistence.TypeMappers._
import com.google.inject.Inject
import com.delphix.eng.dashboard.persistence.Id
import scala.slick.lifted.BaseTypeMapper
import com.delphix.eng.dashboard.revision.Revision
import com.delphix.eng.dashboard.revision.Revisions
import JenkinsJobState._
import JenkinsJobType._
import com.delphix.eng.dashboard.persistence.TypeMappers
import scala.slick.direct.AnnotationMapper.column
import com.delphix.eng.dashboard.persistence.RepoTable

class JenkinsJobs @Inject() (val db: Database, val revisions: Revisions) {

  implicit val stateTypeMapper = MappedTypeMapper.base[JenkinsJobState, String](
    { state => state.toString() },
    { name => JenkinsJobState withName name })

  implicit val jobTypeTypeMapper = MappedTypeMapper.base[JenkinsJobType, String](
    { typeEnum => typeEnum.toString() },
    { name => JenkinsJobType withName name })

  case class NewJenkinsJob(url: Option[String],
    state: Option[JenkinsJobState],
    jobType: JenkinsJobType,
    revision: Id[Revision])
    
  val table = new RepoTable[JenkinsJob]("jenkins_job")(TypeMappers.jenkinsJobTypeMapper) {
    def url = column[Option[String]]("URL")
    def state = column[Option[JenkinsJobState]]("STATE")
    def jobType = column[JenkinsJobType]("JOB_TYPE")
    def revision = column[Id[Revision]]("REVISION_ID")
    def revisionFK = foreignKey("revision_fk", revision, revisions.table)(_.id)
    def * = id.? ~ url ~ state ~ jobType ~ revision <> (JenkinsJob, JenkinsJob.unapply _)
    def autoInc = url ~ state ~ jobType ~ revision <> (NewJenkinsJob, NewJenkinsJob.unapply _) returning id
  }

  def createDDl() = {
    db withSession {
      table.ddl.create
    }
  }

  def get(id: Id[JenkinsJob]) = {
    db withSession {
      (for (f <- table if f.id.asColumnOf[Int] === id.id) yield f) first
    }
  }

  def save(url: Option[String], state: Option[JenkinsJobState], jobType: JenkinsJobType,
      revision: Id[Revision]): Id[JenkinsJob] = {
    db withSession {
      table.autoInc.insert(NewJenkinsJob(url, state, jobType, revision))
    }
  }

  def updateUrl(id: Id[JenkinsJob], url: String) = {
    db withSession {
      val q = for (f <- table if f.id.asColumnOf[Int] === id.id) yield f.url
      val r = q.update(Some(url))
      require(r == 1)
    }
  }
  
  def updateState(id: Id[JenkinsJob], state: JenkinsJobState) = {
    db withSession {
      val q = for (f <- table if f.id.asColumnOf[Int] === id.id) yield f.state
      val r = q.update(Some(state))
      require(r == 1)
    }
  }

  def listByRevision(revId: Id[Revision]) = {
    db withSession {
      (for (
        f <- table if f.revision.asColumnOf[Int] === revId.id
      ) yield f) list
    }
  }

  def listByRevisions(revIds: Seq[Id[Revision]]) = {
    db withSession {
      (for (
        f <- table if f.revision.asColumnOf[Int] inSet (revIds.map(_.id))
      ) yield f) list
    }
  }
  
  def deleteForRevision(revId: Id[Revision]) = {
    db withSession {
     (for (
         f <- table if f.revision.asColumnOf[Int] === revId.id)
       yield f) delete
    }
  }

  def listPending(): List[JenkinsJob] = {
    val PENDING_STATES = List(JenkinsJobState.UNKNOWN, JenkinsJobState.BUILDING,
      JenkinsJobState.REBUILDING, JenkinsJobState).map { _.toString }
    db withSession {
      (for (f <- table
          if (f.state.asColumnOf[String] inSet PENDING_STATES)
    		  || f.state.isNull) yield f) list
    }
  }

  def list(): List[JenkinsJob] = {
    db withSession {
      Query(table).list
    }
  }
}
