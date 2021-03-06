/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.git.repository;

import com.delphix.eng.dashboard.git.commit.Commit
import com.delphix.eng.dashboard.git.commit.CommitId
import com.delphix.eng.dashboard.vm.VmIdentifier
import scala.sys.process.Process
import com.delphix.eng.dashboard.git.commit.Author

class LocalRepo {

  def read(id: CommitId): Commit = {
    val author = new Author(execute("git", "log", "-1", "--format=%ce", id.id).mkString("\n"))
    val msg = execute("git", "log", "-1", "--format=%s", id.id).mkString("\n")
    return new Commit(id, author, msg)
  }

  /**
   * Push to a branch and return the branch name
   */
  def push(commitId: CommitId, vm: VmIdentifier): String = {
    val REMOTE_BRANCH = "refs/hidden/eng-dashboard"
    val APP_GATE = "/export/home/delphix/dlpx-app-gate"
    val host = s"${vm.id}.dcenter.delphix.com"
    execute("git", "push", "-f", s"ssh://delphix@${host}${APP_GATE}", s"${commitId.id}:${REMOTE_BRANCH}")
    return REMOTE_BRANCH
  }

  /**
   * Make a hidden commit visible to downstream repos by creating a branch.
   */
  def makeCommitVisible(commitId: CommitId) = {
    val branchName = "b" + commitId.id
    val out = execute("git", "checkout", "-f", "-B",  branchName, commitId.id)
    branchName
  }

  private def execute(command: String*): List[String] = {
    println(s"running ${command.mkString(" ")}...")
    val ret = Process(command).lines
    println(s"command ${command.mkString(" ")} returned\n${ret.mkString("\n")}")
    return ret.toList
  }
}
