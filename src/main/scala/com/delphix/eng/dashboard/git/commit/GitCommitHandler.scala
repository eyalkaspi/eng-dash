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
import com.delphix.eng.dashboard.vm.VmIdentifier
import com.delphix.eng.dashboard.jenkins.JenkinsJobMonitor
import com.delphix.eng.dashboard.vm.VmIdentifier
import com.delphix.eng.dashboard.command.SshCommandExecutor
import com.delphix.eng.dashboard.command.SshSessionFactory
import com.delphix.eng.dashboard.vm.VmIdentifier

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
    System.out.println("handling " + commitId)
    val commit = localRepo.read(commitId)
    System.out.println("got " + commit)

    val revisionId = revisions.save(commitId)

    jobExecutor.schedule(() => {
      // TODO: need to persist the VM to make sure we unregister it later on
      val vm = dcenter.createVM(revisionId);
      //val vm = new VmIdentifier("eyal-eng-dashboard-1")
      val branch = localRepo.push(commitId, vm)
      
      ssh.withSession(vm) {s =>
        // TODO: need a better way to combine statements
        s.execute(s"cd dlpx-app-gate && git checkout ${branch}")
      }
      
      val preCommitJob = jenkins.runPrecommit(vm, revisionId)
      jenkinsJobs.save(preCommitJob)
      jobMonitor.monitor(preCommitJob)
      
      // TODO Have a background worker monitor the job

    }, commitId)
  }
}
