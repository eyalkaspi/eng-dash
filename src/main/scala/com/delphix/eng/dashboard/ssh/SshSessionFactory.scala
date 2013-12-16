/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.ssh

import com.google.inject.Inject
import com.delphix.eng.dashboard.vm.VmIdentifier
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.IOUtils
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.connection.channel.direct.Session.Command
import java.io.IOException
import java.util.concurrent.TimeUnit
import net.schmizz.sshj.transport.verification.HostKeyVerifier
import java.security.PublicKey

class SshSessionFactory {

  private class AlwaysYesHostKeyVerifier extends HostKeyVerifier {
    def verify(hostname: String, port: Int, key: PublicKey): Boolean = true
  }

  private class SshExecImpl(val ssh: SSHClient, val hostName: String) extends SshExec {
    override def exec(command: String*): String = {
      exec({ (exitCode: Int, stdout: String, stderr: String) =>
        throw new IllegalStateException(s"Non zero exit code for ${command.mkString(" ")} on host ${hostName}")
      }, command:_*)
    }
    
    override def exec(exceptionHandler: (Int, String, String) => Unit, command: String*): String = {
      // TODO: find a way to reuse the session
      val cmdAsString = command.mkString(" ")
      println(s"executing over ssh ${cmdAsString} on host ${hostName}")
      val session = ssh.startSession()
      try {
        val cmd = session.exec(cmdAsString)
        cmd.join()
        val stdout = IOUtils.readFully(cmd.getInputStream()).toString()
        val stderr = IOUtils.readFully(cmd.getErrorStream()).toString()
        println(s"stdout ${stdout}")
        println(s"stderr ${stderr}")
        if (cmd.getExitStatus() != 0) {
          exceptionHandler(cmd.getExitStatus(), stdout, stderr)
        }
        return stdout
      } finally {
        session.close();
      }
    }
  }

  def withSession[T](vmId: VmIdentifier)(f: (SshExec) => T): T = {
    withSession(vmId.hostName)(f)
  }

  def withSession[T](hostName: String)(f: (SshExec) => T): T = {
    val ssh: SSHClient = new SSHClient()
    ssh.addHostKeyVerifier(new AlwaysYesHostKeyVerifier())
    ssh.connect(hostName)
    try {
      println(s"ssh'ing to ${hostName}")
      val base = s"${System.getProperty("user.home")}/.ssh/";
      hostName match {
        case "dcenter" => ssh.authPublickey("eyal", base + "id_rsa")
        case _ => ssh.authPublickey("delphix", base + "id_rsa_jenkins")
      }
      f(new SshExecImpl(ssh, hostName))
    } finally {
      ssh.disconnect();
    }
  }
}