/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.jenkins

import scala.slick.driver.H2Driver.simple._
import scala.slick.driver.H2Driver.simple.Database.threadLocalSession
import com.delphix.eng.dashboard.persistence.TypeMappers._
import com.google.inject.Inject
import com.delphix.eng.dashboard.persistence.Id
import scala.slick.lifted.BaseTypeMapper
import com.delphix.eng.dashboard.revision.Revision
import com.delphix.eng.dashboard.revision.Revisions
import JenkinsJobState._
import JenkinsJobType._

class JenkinsJobs @Inject() (val db: Database, val revisions: Revisions) {

  implicit val stateTypeMapper = MappedTypeMapper.base[JenkinsJobState, String](
    { state => state.toString() },
    { name => JenkinsJobState withName name })

  implicit val jobTypeTypeMapper = MappedTypeMapper.base[JenkinsJobType, String](
    { typeEnum => typeEnum.toString() },
    { name => JenkinsJobType withName name })

  val table = new Table[JenkinsJob]("JENKINS_JOB") {
    def id = column[Id[JenkinsJob]]("ID", O.PrimaryKey)
    def url = column[String]("URL")
    def state = column[JenkinsJobState]("STATE")
    def jobType = column[JenkinsJobType]("JOB_TYPE")
    def revision = column[Id[Revision]]("REVISION_ID")
    def revisionFK = foreignKey("revision_fk", revision, revisions.table)(_.id)
    def * = id ~ url ~ state ~ jobType ~ revision <> (JenkinsJob, JenkinsJob.unapply _)
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

  def save(job: JenkinsJob): Unit = {
    db withSession {
      table.insert(job)
    }
  }

  def updateState(id: Id[JenkinsJob], state: JenkinsJobState) = {
    db withSession {
      val q = for (f <- table if f.id.asColumnOf[Int] === id.id) yield f.state
      val r = q.update(state)
      require(r == 1)
    }
  }

  def listByRevision(revId: Id[Revision]) = {
    db withSession {
      (for (f <- table if f.revision.asColumnOf[Int] === revId.id) yield f) list
    }
  }

  def listPending(): List[JenkinsJob] = {
    val PENDING_STATES = List(JenkinsJobState.UNKNOWN, JenkinsJobState.BUILDING, 
        JenkinsJobState.REBUILDING, JenkinsJobState).map{_.toString}
    db withSession {
      (for (f <- table if f.state.asColumnOf[String] inSet PENDING_STATES) yield f) list
    }
  }
  
  def list(): List[JenkinsJob] = {
    db withSession {
      Query(table).list
    }
  }
}
