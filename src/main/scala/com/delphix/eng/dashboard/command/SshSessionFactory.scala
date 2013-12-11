package com.delphix.eng.dashboard.command

import com.google.inject.Inject
import com.delphix.eng.dashboard.vm.VmIdentifier

class SshSessionFactory @Inject() (val localExec: LocalCommandExecutor) {
  
  def withSession[T](vmId: VmIdentifier) (f: (SshCommandExecutor) => T): T = {
    // TODO: Provide an elegant way to group commands
    f(new WithGitDirSshCommandExecutor(new SshCommandExecutor(vmId.hostName, localExec)))
  }
  
  // TODO: refactor to use delegation only (not inheritance)
  private class WithGitDirSshCommandExecutor(val delegate: SshCommandExecutor) extends 
  	SshCommandExecutor(delegate.host, delegate.localExecutor) {
    
    override def execute(command: List[String]): Seq[String] = {
      delegate.execute("GIT_DIR=/export/home/delphix/dlpx-app-gate/.git" :: command)
    }
  }
  
}