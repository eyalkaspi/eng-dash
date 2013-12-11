package com.delphix.eng.dashboard.command;

import scala.sys.process._

/**
 * Wrapper around scala.sys.process for testability and logging.
 */
trait LocalCommandExecutor {
  def execute(command: List[String]): Seq[String]
}