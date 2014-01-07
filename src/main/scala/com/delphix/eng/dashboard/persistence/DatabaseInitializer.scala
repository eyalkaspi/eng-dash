/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.persistence

import scala.slick.session.Database
import com.google.inject.Inject
import com.delphix.eng.dashboard.revision.Revisions
import scala.slick.driver.PostgresDriver.simple._
import scala.slick.driver.PostgresDriver.simple.Database.threadLocalSession
import scala.slick.lifted.BaseTypeMapper
import com.delphix.eng.dashboard.git.commit.CommitId
import com.delphix.eng.dashboard.persistence.TypeMappers._
import com.google.inject.Inject
import com.delphix.eng.dashboard.git.commit.CommitId
import com.delphix.eng.dashboard.revision.Revisions
import com.delphix.eng.dashboard.jenkins.JenkinsJobs
import com.delphix.eng.dashboard.git.repository.LocalRepo
import scala.slick.jdbc.StaticQuery

class DatabaseInitializer {

  @Inject val db: Database = null
  @Inject val revisions: Revisions = null
  @Inject val jenkinsJobs: JenkinsJobs = null
  @Inject val localRepo: LocalRepo = null

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
  /*  db.withSession {
      try {
        StaticQuery.updateNA("alter table REVISION add column REV_TYPE TEXT").execute
        StaticQuery.updateNA("update REVISION set REV_TYPE='TESTING' where REV_TYPE IS NULL").execute
      } catch {
        case e: Exception =>
          // TODO: Need a way to run that only if ddl does not exist
          e.printStackTrace()
      }
    }*/
  }
}