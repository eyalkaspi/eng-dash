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
import scala.beans.BeanProperty
import com.delphix.eng.dashboard.jenkins.JenkinsJob
import com.delphix.eng.dashboard.jenkins.JenkinsJobType
import com.delphix.eng.dashboard.jenkins.JenkinsJobs

@Path("/revisions")
class RevisionResource @Inject() (
  val revisons: Revisions,
  val jenkinsJobs: JenkinsJobs) {

  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  def list(): java.util.List[RevisionsView] = {
    val revs = revisons.list
    val jobsMap = jenkinsJobs.listByRevisions(revs.map { _.id.get }).groupBy(_.revision)
    val revJson = revs.map { r =>
      val jobs: Option[List[JenkinsJob]] = jobsMap.get(r.id.get)
      val precommit = jobs map { _.find { _.jobType == JenkinsJobType.PRECOMMIT } }
      val blackbox = jobs map { _.find { _.jobType == JenkinsJobType.BLACKBOX } }
      val push = jobs map { _.find { _.jobType == JenkinsJobType.DX_PUSH } }
      new RevisionsView(r, precommit getOrElse None, blackbox getOrElse None, push getOrElse None)
    }
    scala.collection.JavaConversions.asJavaList(revJson)
  }

  class RevisionsView(revision: Revision, preCommit: Option[JenkinsJob],
    blackBox: Option[JenkinsJob], push: Option[JenkinsJob]) {
    def getId = revision.id.get.id
    def getCommitId = revision.commitId.id
    def getState = revision.state.toString()
    def getVm = revision.vm.id
    def getAuthor = revision.author.email.split("@")(0)
    def getPreCommitStatus = getState(preCommit)
    def getPreCommitUrl = getUrl(preCommit)
    def getBlackboxStatus = getState(blackBox)
    def getBlackboxUrl = getUrl(blackBox)
    def getPushStatus = revision.revisionType match {
      case RevisionType.PUSHING =>
        push match {
          case Some(j) => getState(push)
          case _ => "PRE_CREATION"
        }
      case _ => null
    }
    def getPushUrl = getUrl(push)
    def getCommitMsg = revision.commitMsg
    def getType = revision.revisionType.toString()

    private def getState(job: Option[JenkinsJob]): String = {
      job match {
        case Some(j) =>
          j.state match {
            case Some(s) => s.toString()
            case _ => "PRE_CREATION"
          }
        case _ => null
      }
    }
    
    private def getUrl(job: Option[JenkinsJob]): String = {
      job match {
        case Some(j) =>
          j.url match {
            case Some(u) => u.toString()
            case _ => null
          }
        case _ => null
      }
    }
  }
}
