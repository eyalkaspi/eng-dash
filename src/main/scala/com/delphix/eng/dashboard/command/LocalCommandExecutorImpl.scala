package com.delphix.eng.dashboard.command

import scala.sys.process._
import com.google.common.base.Preconditions

class LocalCommandExecutorImpl extends LocalCommandExecutor {
  def execute(command: List[String]): Seq[String] = {
    println(s"running ${command.mkString(" ")}...")
    val ret = Process(command).lines
    println(s"command ${command.mkString(" ")} returned\n${ret.mkString("\n")}")
    return ret
  }
}