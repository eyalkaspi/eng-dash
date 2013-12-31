/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.git.commit;

import com.delphix.eng.dashboard.git.repository.LocalRepo
import com.delphix.eng.dashboard.jobs.JobExecutor
import com.delphix.eng.dashboard.vm.Dcenter
import com.google.inject.Inject
import com.delphix.eng.dashboard.revision.Revisions
import com.delphix.eng.dashboard.jenkins.JenkinsClient
import com.delphix.eng.dashboard.jenkins.JenkinsJobs
import com.delphix.eng.dashboard.vm.VmIdentifier
import com.delphix.eng.dashboard.persistence.Id
import com.delphix.eng.dashboard.revision.Revision
import com.delphix.eng.dashboard.revision.RevisionState
import com.delphix.eng.dashboard.revision.RevisionState._
import com.delphix.eng.dashboard.jenkins.JenkinsClient
import com.delphix.eng.dashboard.jenkins.JenkinsJobMonitor
import com.delphix.eng.dashboard.vm.VmIdentifier
import com.delphix.eng.dashboard.ssh.SshSessionFactory
import com.delphix.eng.dashboard.jenkins.JenkinsJobType._
import com.delphix.eng.dashboard.jenkins.JenkinsJob
import com.delphix.eng.dashboard.jenkins.JenkinsJobState._
import com.delphix.eng.dashboard.jenkins.JenkinsJobState
import com.delphix.eng.dashboard.revision.RevisionType

class GitCommitHandler {

  @Inject val localRepo: LocalRepo = null
  @Inject val dcenter: Dcenter = null
  @Inject val jobExecutor: JobExecutor = null
  @Inject val revisions: Revisions = null
  @Inject val jenkins: JenkinsClient = null
  @Inject val jenkinsJobs: JenkinsJobs = null
  @Inject val ssh: SshSessionFactory = null

  def handleNewCommit(commitId: CommitId, jobTypes: Seq[JenkinsJobType], pushUpstream: Boolean): Unit = {
    println("handling " + commitId)
    val commit = localRepo.read(commitId)
    println("got " + commit)

    val vm = new VmIdentifier("eyal-eng-dashboard-" + commit.id.id.substring(0, 10))
    val revType = if (pushUpstream) RevisionType.PUSHING else RevisionType.TESTING;
    val revisionId = revisions.save(commitId, vm, commit.author, commit.msg, revType)
    runJenkinsJobs(commitId, revisionId, jobTypes, pushUpstream)
  }

  def runJenkinsJobs(commitId: CommitId, revisionId: Id[Revision],
    jobTypes: Seq[JenkinsJobType], pushUpstream: Boolean) = {
    val revision = revisions.get(revisionId)
    val vm = revision.vm
    jobExecutor.schedule(() => {
      revisions.updateState(revisionId, RevisionState.RUNNING)
      try {
        /*
         * Create entries in the jenkins job table immediatly for the front-end
         * to display placeholders
         */
        jobTypes.foreach { jobType =>
          jenkinsJobs.save(None, None, jobType, revisionId)
        }

        dcenter.createVM(vm);

        val branch = localRepo.push(commitId, vm)

        ssh.withSession(vm) { s =>
          s.exec(s"cd /export/home/delphix/dlpx-app-gate/ && git checkout ${branch}")
        }

        val jobResults = jenkins.run(revision, jobTypes)
        if (pushUpstream) {
          if (jobResults.values.find(_ != JenkinsJobState.SUCCESS).isEmpty) {
            val branch = localRepo.makeCommitVisible(revision.commitId)
            jenkins.dxPush(revision, branch)
          }
        }
        revisions.updateState(revisionId, RevisionState.COMPLETE)
        // }
      } catch {
        case e: Exception =>
          e.printStackTrace()
          revisions.updateState(revisionId, RevisionState.FAILED)
      } finally {
        jenkins.suspend(revision)
      }
    })
  }
}
