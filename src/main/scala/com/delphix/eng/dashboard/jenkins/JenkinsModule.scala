/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.jenkins

import com.tzavellas.sse.guice.ScalaModule
import com.google.inject.Provides
import com.offbytwo.jenkins.JenkinsServer
import com.google.inject.Singleton
import java.net.URI
import com.offbytwo.jenkins.client.JenkinsHttpClient
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.client.LaxRedirectStrategy

class JenkinsModule extends ScalaModule {

  def configure() {
  }

  @Provides
  @Singleton
  def jenkinsClient() = {
    val httpClient = new JenkinsHttpClient(new URI("http://jenkins/"), "eyal.kaspi@delphix.com", "cb50dd0626dacb663f1dcca8d0865afb")
    new JenkinsClient(new JenkinsServer(httpClient), httpClient)
  }

}