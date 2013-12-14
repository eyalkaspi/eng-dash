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
import com.delphix.eng.dashboard.jenkins.JenkinsClient
import com.delphix.eng.dashboard.jenkins.JenkinsJobMonitor
import com.delphix.eng.dashboard.vm.VmIdentifier
import com.delphix.eng.dashboard.ssh.SshSessionFactory

class GitCommitHandler {

  @Inject val localRepo: LocalRepo = null
  @Inject val dcenter: Dcenter = null
  @Inject val jobExecutor: JobExecutor = null
  @Inject val revisions: Revisions = null
  @Inject val jenkins: JenkinsClient = null
  @Inject val ssh: SshSessionFactory = null
  @Inject val jobMonitor: JenkinsJobMonitor = null

  def retryCommit(commitId: CommitId, revision: Revision) = {
    println("retrying" + commitId)
    val commit = localRepo.read(commitId)
    println("got " + commit)

    runJob(commitId, revision.id.get)
  }

  def handleNewCommit(commitId: CommitId): Unit = {
    println("handling " + commitId)
    val commit = localRepo.read(commitId)
    println("got " + commit)

    val vm = new VmIdentifier("eyal-eng-dashboard-" + commit.id.id.substring(0,10))
    val revisionId = revisions.save(commitId, vm, commit.author)

    runJob(commitId, revisionId)
  }

  def runJob(commitId: CommitId, revisionId: Id[Revision]) = {
    val revision = revisions.get(revisionId)
    val vm = revision.vm
    jobExecutor.schedule(() => {
      revisions.updateState(revisionId, RevisionState.RUNNING)
      try {
        dcenter.createVM(vm);

        val branch = localRepo.push(commitId, vm)

        ssh.withSession(vm) { s =>
          s.exec(s"cd /export/home/delphix/dlpx-app-gate/ && git checkout ${branch}")
        }

        jobMonitor.monitor(jenkins.runPrecommit(vm, revision))
        jobMonitor.monitor(jenkins.runBlackBox(vm, revision))
      } catch {
        case e: Exception =>
          e.printStackTrace()
          revisions.updateState(revisionId, RevisionState.FAILED)
          jenkins.suspend(vm)
      }

    }, commitId)
  }
}
