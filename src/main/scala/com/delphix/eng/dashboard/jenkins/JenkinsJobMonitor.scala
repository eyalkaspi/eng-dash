/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.jenkins

import com.delphix.eng.dashboard.time.Sleepy
import com.offbytwo.jenkins.model.BuildResult
import com.delphix.eng.dashboard.revision.Revisions
import com.delphix.eng.dashboard.git.commit.GitCommitHandler
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import com.google.inject.Inject
import scala.collection.JavaConversions._
import com.delphix.eng.dashboard.vm.VmIdentifier
import com.delphix.eng.dashboard.revision.RevisionState
import com.delphix.eng.dashboard.persistence.Id
import com.delphix.eng.dashboard.revision.Revision
import com.google.inject.Singleton

@Singleton
class JenkinsJobMonitor @Inject() (
  val jenkins: JenkinsClient,
  val sleepy: Sleepy,
  val jenkinsJobs: JenkinsJobs,
  val revisions: Revisions) {

  def monitor(job: JenkinsJob) = {

    sleepy.runAtFixedInterval(10, TimeUnit.SECONDS, (f) => {
      try {
        val details = jenkins.getJobDetails(job)
        // TODO
        //job.setState(details.getResult())
        //jenkinsJobs.update(job)
        if (details.isBuilding()) {
          println(s"job ${job.id} building")
        } else {
          println("removed")
          jobComplete(job.revision)
          // prevent future executions
          f.cancel(false)
        }
      } catch {
        case e: Exception => e.printStackTrace()
      }
    })
  }
  
  def start() = {
    // TODO: load pending jobs from database and schedule monitoring tasks
  }

  private def jobComplete(revisionId: Id[Revision]) = {
    val revision = revisions.get(revisionId)
    // TODO: support multiple jobs

    // Update database
    revisions.updateState(revisionId, RevisionState.COMPLETE)

    // Suspend VM
    // TODO: store the vm identifier along with the revision
    val vmId = new VmIdentifier("eyal-eng-dashboard-" + revisionId.id)
    jenkins.suspend(vmId)

    // TODO: Not sure if we should send an email, since jenkins already is

  }

}