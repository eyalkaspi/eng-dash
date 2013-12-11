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

class JenkinsJobs @Inject() (val db: Database, val revisions: Revisions) {

  implicit val stateTypeMapper = MappedTypeMapper.base[JenkinsJobState, String](
    { state => state.toString() },
    { name => JenkinsJobState withName name })

  val table = new Table[JenkinsJob]("JENKINS_JOB") {
    def id = column[Id[JenkinsJob]]("ID", O.PrimaryKey)
    def url = column[String]("URL")
    def state = column[JenkinsJobState]("STATE")
    def revision = column[Id[Revision]]("REVISION_ID")
    def revisionFK = foreignKey("revision_fk", revision, revisions.table)(_.id)
    def * = id ~ url ~ state ~ revision <> (JenkinsJob, JenkinsJob.unapply _)
  }

  def createDDl() = {
    db withSession {
      table.ddl.create
    }
  }

  def save(job: JenkinsJob): Unit = {
    db withSession {
      table.insert(job)
    }
  }

  def list(): List[JenkinsJob] = {
    db withSession {
      Query(table).list
    }
  }
}
