/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.revision

import javax.ws.rs.GET
import javax.ws.rs.Produces
import javax.ws.rs.Path
import com.google.inject.Inject
import javax.ws.rs.core.MediaType
import java.util.List
import scala.collection.JavaConversions._
import scala.beans.BeanProperty
import com.delphix.eng.dashboard.jenkins.JenkinsJob
import com.delphix.eng.dashboard.jenkins.JenkinsJob
import com.delphix.eng.dashboard.jenkins.JenkinsJobType
import com.delphix.eng.dashboard.jenkins.JenkinsJobs

@Path("/revisions")
class RevisionResource @Inject() (
    val revisons: Revisions,
    val jenkinsJobs: JenkinsJobs) {
  
  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  def list(): List[RevisionsView] = {
    revisons.list.map { r =>
      val jobs = jenkinsJobs.listByRevision(r.id.get)
      new RevisionsView(r, 
          jobs.find { _.jobType == JenkinsJobType.PRECOMMIT },
          jobs.find { _.jobType == JenkinsJobType.BLACKBOX })
    }
  }
  
  class RevisionsView(revision: Revision, preCommit: Option[JenkinsJob],
      blackBox: Option[JenkinsJob]) {
    def getId = revision.id.get.id
    def getCommitId = revision.commitId.id
    def getState = revision.state.toString()
    def getVm = revision.vm.id
    def getAuthor = revision.author.email
    def getPreCommitStatus = preCommit.map{_.state.toString()}.orNull
    def getPreCommitUrl = preCommit.map{_.url}.orNull
    def getBlackboxStatus = blackBox.map{_.state.toString()}.orNull
    def getBlackboxUrl = blackBox.map{_.url}.orNull
  }
}
