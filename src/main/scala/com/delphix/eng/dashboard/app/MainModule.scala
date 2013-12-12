/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.app

import com.google.inject.AbstractModule
import com.sun.jersey.guice.JerseyServletModule
import com.delphix.eng.dashboard.git.commit.CommitResource
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer
import com.delphix.eng.dashboard.persistence.SlickModule
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
    
    // jenkins integration
    install(new JenkinsModule())
  }
}