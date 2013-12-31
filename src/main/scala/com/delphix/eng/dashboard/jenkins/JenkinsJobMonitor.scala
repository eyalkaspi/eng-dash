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
import com.delphix.eng.dashboard.jobs.JobExecutor

class JenkinsJobMonitor @Inject() (
  val jenkins: JenkinsClient,
  val jenkinsJobs: JenkinsJobs,
  val jobExecutor: JobExecutor,
  val revisions: Revisions) {

  def start() = {
    jenkinsJobs.listPending().foreach { job =>
      jobExecutor.schedule { () =>
        /* TODO: Need to be able to retrieve or persist the task schedule to restart it.
	     * As a quick fix, just make sure we cleanup the VM
	     */

        jenkins.monitorAndWait(List(job.id.get))
        jenkins.suspend(revisions.get(job.revision))
      }
    }
  }

}