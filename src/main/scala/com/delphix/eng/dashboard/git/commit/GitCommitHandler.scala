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
  @Inject val jenkinsJobs: JenkinsJobs = null
  @Inject val jobMonitor: JenkinsJobMonitor = null
  @Inject val ssh: SshSessionFactory = null

  def handleNewCommit(commitId: CommitId): Unit = {
    println("handling " + commitId)
    val commit = localRepo.read(commitId)
    println("got " + commit)

    val revisionId = revisions.save(commitId)

    jobExecutor.schedule(() => {
      val vm = dcenter.createVM(revisionId);
      val branch = localRepo.push(commitId, vm)

      ssh.withSession(vm) { s =>
        s.exec(s"cd /export/home/delphix/dlpx-app-gate/ && git checkout ${branch}")
      }

      val preCommitJob = jenkins.runPrecommit(vm, revisionId)
      jenkinsJobs.save(preCommitJob)
      jobMonitor.monitor(preCommitJob)

    }, commitId)
  }
}
