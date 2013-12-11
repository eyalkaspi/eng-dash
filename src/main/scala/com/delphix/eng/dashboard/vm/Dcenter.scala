/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.vm;

import com.delphix.eng.dashboard.command.SshCommandExecutor
import com.delphix.eng.dashboard.git.commit.CommitId
import com.google.inject.Inject
import com.google.inject.name.Named
import com.delphix.eng.dashboard.command.LocalCommandExecutor
import com.delphix.eng.dashboard.persistence.Id
import com.delphix.eng.dashboard.revision.Revision
import com.delphix.eng.dashboard.time.Sleepy
import java.util.concurrent.TimeUnit
import com.delphix.eng.dashboard.command.SshSessionFactory

class Dcenter {

  @Inject
  @Named("dcenter")
  val dcenter: SshCommandExecutor = null

  @Inject
  val ssh: SshSessionFactory = null

  @Inject
  val cmdExec: LocalCommandExecutor = null

  @Inject
  val sleepy: Sleepy = null

  def createVM(id: Id[Revision]): VmIdentifier = {
    // TODO: this should be resolved from the commit
    val groupName = "dlpx-trunk"
    val vmName = "eyal-eng-dashboard-" + id.id
    val vm = new VmIdentifier(vmName)

    // TODO: Set an automatic expiration in 
    dc("clone-latest", groupName, vmName)
    Thread.sleep(1000)
    dc("guest wait", vmName)

    // The VM is not ready, even after dc guest wait
    sleepy.retry(10, (10, TimeUnit.SECONDS)) { () =>
      ssh.withSession(vm) {
        // TODO: can we use a dc guest command to query smf instead (if ssh is not ready)
        _.execute("test -d /export/home/delphix/dlpx-app-gate/")
      }
    }

    return vm
  }

  private def dc(cmd: String*): Unit = {
    dcenter.execute((List("/usr/bin/dc") ++ cmd): _*)
  }
}
