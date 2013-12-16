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
import com.sun.jersey.spi.container.WebApplication
import java.util.Map
import java.util.Collections
import com.delphix.eng.dashboard.revision.RevisionResource

class MainModule extends JerseyServletModule {
  override def configureServlets() = {
    // Bind all resources
    bind(classOf[CommitResource]);
    bind(classOf[RevisionResource]);

    /*
	 * Route all requests through GuiceContainer
	 * Note that with is a scala keyword
	 */
    val initParams =
      Collections.singletonMap("com.sun.jersey.api.json.POJOMappingFeature", "true")
    serve("/resource/*").`with`(classOf[GuiceContainer], initParams);
    
    // database module
    install(new SlickModule())
    
    // jenkins integration
    install(new JenkinsModule())
  }
  
}