package com.delphix.eng.dashboard.command

import org.scalatest.FunSuite
import org.scalamock.scalatest.MockFactory
import com.google.common.net.InetAddresses

class SshCommandExecutorTest extends FunSuite with MockFactory {
  
  test("ssh command") {
    val localExec = mock[LocalCommandExecutor]
    (localExec.execute _).
    	expects(List("/bin/bash", "-c", "ssh host 'someCommand'")).
    	returning(List("some", "output"))
    
    val sshExec = new SshCommandExecutor("host", localExec)
    val out = sshExec.execute(List("someCommand"))
    assert(out == List("some", "output"))
  }
}