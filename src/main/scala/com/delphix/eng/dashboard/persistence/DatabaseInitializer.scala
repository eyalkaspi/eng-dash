package com.delphix.eng.dashboard.persistence

import scala.slick.session.Database
import com.google.inject.Inject
import com.delphix.eng.dashboard.revision.Revisions
import scala.slick.driver.H2Driver.simple._
import scala.slick.driver.H2Driver.simple.Database.threadLocalSession
import scala.slick.lifted.BaseTypeMapper
import com.delphix.eng.dashboard.git.commit.CommitId
import com.delphix.eng.dashboard.persistence.TypeMappers._
import com.google.inject.Inject
import com.delphix.eng.dashboard.git.commit.CommitId
import com.delphix.eng.dashboard.revision.Revisions
import com.delphix.eng.dashboard.jenkins.JenkinsJobs

class DatabaseInitializer {

  @Inject val db: Database = null
  @Inject val revisions: Revisions = null
  @Inject val jenkinsJobs: JenkinsJobs = null

  def initialize() = {
    db.withSession {
      try {
        revisions.createDDl
        jenkinsJobs.createDDl
      } catch {
        case e: Exception =>
          // TODO: Need a way to run that only if ddl does not exist
          e.printStackTrace()
      }
    }
  }
}