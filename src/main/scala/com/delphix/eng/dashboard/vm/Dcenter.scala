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

  def createVM(id: Id[Revision]): VmIdentifier = {
    // TODO: this should be resolved from the commit
    val groupName = "dlpx-trunk"
    val vmName = "eyal-eng-dashboard-" + id.id
    val vm = new VmIdentifier(vmName)

    // TODO: Set an automatic expiration in
    ssh.withSession("dcenter") { s =>
      s.exec("/usr/bin/dc clone-latest", groupName, vmName)
      // The VM is not ready, even after dc guest wait
      sleepy.retry(10, (10, TimeUnit.SECONDS)) { () =>
        s.exec(s"/usr/bin/dc guest wait", vmName)
        s.exec(s"/usr/bin/dc guest run", vmName, "'svcadm enable -s svc:/network/ssh:default'")
      }
    }

    return vm
  }
}
