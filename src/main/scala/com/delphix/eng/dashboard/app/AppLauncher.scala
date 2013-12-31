/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.app

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import com.delphix.eng.dashboard.jenkins.JenkinsClient
import com.delphix.eng.dashboard.jenkins.JenkinsJob
import com.delphix.eng.dashboard.jenkins.JenkinsJobMonitor
import com.delphix.eng.dashboard.persistence.DatabaseInitializer
import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.name.Named
import com.google.inject.servlet.GuiceFilter

object AppLauncher {

  @Inject val dbInitializer: DatabaseInitializer = null
  @Inject val jenkinsMonitor: JenkinsJobMonitor = null
  @Inject @Named("port") val port: Integer = null
  @Inject val jenkinsClient: JenkinsClient = null

  def main(args: Array[String]) {
    // Init Guice
    val injector = Guice.createInjector(new MainModule())
    injector.injectMembers(this)

    // Initialize the database
    dbInitializer.initialize()

    // Start monitoring jenkings
    jenkinsMonitor.start()

    // Create the server.
    val server = new Server(port);

    // Create a servlet context and add the jersey servlet.
    val sch = new ServletContextHandler(server, "/");

    // Add our Guice listener that includes our bindings
    sch.addEventListener(new ServletConfig(injector));

    // Then add GuiceFilter and configure the server to
    // reroute all requests through this filter.
    sch.addFilter(classOf[GuiceFilter], "/*", null);

    // Must add DefaultServlet for embedded Jetty.
    // Failing to do this will cause 404 errors.
    // This is not needed if web.xml is used instead.
    sch.addServlet(classOf[StaticServlet], "/");

    // Start the server
    server.start();
    server.join();
  }
}