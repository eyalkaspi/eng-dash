/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.command

import com.tzavellas.sse.guice.ScalaModule
import com.google.inject.Provides
import com.google.inject.name.Named
import java.net.UnknownHostException
import com.google.common.base.Throwables

class CommandExecutorModule extends ScalaModule {
  def configure() {
    bind[LocalCommandExecutor].to[LocalCommandExecutorImpl]
  }

  @Provides
  @Named("dcenter")
  def dcenterRemoteExecutor(localExecutor: LocalCommandExecutor) = {
    try {
      new SshCommandExecutor("dcenter", localExecutor);
    } catch {
      case e: UnknownHostException => throw Throwables.propagate(e);
    }
  }
}