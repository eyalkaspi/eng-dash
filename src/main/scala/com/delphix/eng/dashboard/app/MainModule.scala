package com.delphix.eng.dashboard.app

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.sun.jersey.guice.JerseyServletModule
import com.delphix.eng.dashboard.command.SshCommandExecutor
import com.google.common.base.Throwables
import com.delphix.eng.dashboard.command.LocalCommandExecutor
import com.google.inject.name.Named
import java.io.File
import java.net.UnknownHostException
import com.delphix.eng.dashboard.git.commit.CommitResource
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer
import com.delphix.eng.dashboard.persistence.SlickModule
import java.net.InetAddress
import com.delphix.eng.dashboard.command.CommandExecutorModule
import com.delphix.eng.dashboard.jenkins.JenkinsModule

class MainModule extends JerseyServletModule {
  override def configureServlets() = {
    // Must configure at least one JAX-RS resource or the
    // server will fail to start.
    bind(classOf[CommitResource]);

    /*
		 * Route all requests through GuiceContainer
		 * Note that with is a scala keyword
		 */
    serve("/*").`with`(classOf[GuiceContainer]);
    
    // database module
    install(new SlickModule())
    
    // local and remote execution module
    install(new CommandExecutorModule())
    
    // jenkins integration
    install(new JenkinsModule())
  }
}