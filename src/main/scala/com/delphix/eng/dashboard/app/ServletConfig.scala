package com.delphix.eng.dashboard.app

import java.io.File
import java.net.InetAddress
import java.net.UnknownHostException
import com.delphix.eng.dashboard.command.LocalCommandExecutor
import com.delphix.eng.dashboard.command.SshCommandExecutor
import com.delphix.eng.dashboard.git.commit.CommitResource
import com.google.common.base.Throwables
import com.google.inject.Guice
import com.google.inject.Provides
import com.google.inject.name.Named
import com.google.inject.servlet.GuiceServletContextListener
import com.sun.jersey.guice.JerseyServletModule
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer
import com.delphix.eng.dashboard.git.commit.CommitResource
import com.delphix.eng.dashboard.persistence.SlickModule
import javax.servlet.ServletContextEvent
import com.google.inject.Injector

class ServletConfig(injector: Injector) extends GuiceServletContextListener {

  override def getInjector() = injector

}