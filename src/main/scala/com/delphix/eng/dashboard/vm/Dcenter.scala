/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.vm;

import com.delphix.eng.dashboard.git.commit.CommitId
import com.google.inject.Inject
import com.google.inject.name.Named
import com.delphix.eng.dashboard.persistence.Id
import com.delphix.eng.dashboard.revision.Revision
import com.delphix.eng.dashboard.time.Sleepy
import java.util.concurrent.TimeUnit
import com.delphix.eng.dashboard.ssh.SshSessionFactory

class Dcenter {

  @Inject
  val ssh: SshSessionFactory = null

  @Inject
  val sleepy: Sleepy = null

  def createVM(vm: VmIdentifier): VmIdentifier = {
    // TODO: this should be resolved from the commit
    val groupName = "dlpx-trunk"

    // TODO: Set an automatic expiration when cloning
    ssh.withSession("dcenter") { s =>
      s.exec({ (exitCode: Int, stdout: String, stderr: String) =>
        if (!stderr.contains("already exists")) {
          throw new IllegalArgumentException("failed to clone VM")
        }
        // TODO: Check if it is registered before trying
        s.exec({ (exitCode: Int, stdout: String, stderr: String) =>
          if (!stderr.contains("already registere")) {
            throw new IllegalArgumentException("failed to clone VM")
          }
        }, "/usr/bin/dc register", vm.id)
      }, "/usr/bin/dc clone-latest", groupName, vm.id)
      // The VM is not ready, even after dc guest wait
      sleepy.retry(10, (10, TimeUnit.SECONDS)) { () =>
        s.exec(s"/usr/bin/dc guest wait", vm.id)
        s.exec(s"/usr/bin/dc guest run", vm.id, "'svcadm enable -s svc:/network/ssh:default'")
      }
    }

    return vm
  }
}
