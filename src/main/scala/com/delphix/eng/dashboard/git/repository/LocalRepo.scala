package com.delphix.eng.dashboard.git.repository;

import com.delphix.eng.dashboard.command.LocalCommandExecutor
import com.delphix.eng.dashboard.git.commit.Commit
import com.delphix.eng.dashboard.git.commit.CommitId
import com.google.inject.Inject
import com.google.inject.name.Named
import com.delphix.eng.dashboard.vm.VmIdentifier

class LocalRepo {

  @Inject val commandExecutor: LocalCommandExecutor = null

  def read(id: CommitId): Commit = {
    return Commit(execute("git", "log", "-1", id.id))
  }
  
  /**
   * Push to a branch and return the branch name
   */
  def push(commitId: CommitId, vm: VmIdentifier): String = {
    val REMOTE_BRANCH = "refs/hidden/eng-dashboard"
    val APP_GATE="/export/home/delphix/dlpx-app-gate"
    val host = s"${vm.id}.dcenter.delphix.com"
    execute("git", "push", "-f", s"ssh://delphix@${host}${APP_GATE}", s"${commitId.id}:${REMOTE_BRANCH}")
    return REMOTE_BRANCH
  }

  private def execute(command: String*) = {
    commandExecutor.execute(command.toList)
  }
}
