/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.command;

class SshCommandExecutor(val host: String, val localExecutor: LocalCommandExecutor) {

  def execute(command: String*): Seq[String] = {
    execute(command.toList)
  }
  
  def execute(command: List[String]): Seq[String] = {
    localExecutor.execute(List("/usr/bin/ssh", "-T", host, s"${command.mkString(" ")}"))
  }
}
