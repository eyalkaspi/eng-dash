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
import org.apache.http.HttpHost
import org.apache.http.impl.conn.PoolingClientConnectionManager
import org.apache.http.impl.client.AbstractHttpClient

class JenkinsModule extends ScalaModule {

  def configure() {
  }

  @Provides
  def jenkinsHttpClient(): JenkinsHttpClient = {
    // Need an http client that supports concurrent connections
    val cm = new PoolingClientConnectionManager()
    // Increase max total connection to 10
    cm.setMaxTotal(10)
    cm.setDefaultMaxPerRoute(5)

    val jenkinsClient = 
      new JenkinsHttpClient(new URI("http://jenkins/"),
          "eyal.kaspi@delphix.com", "cb50dd0626dacb663f1dcca8d0865afb")
    // Set the http client through reflection
    val field = classOf[JenkinsHttpClient].getDeclaredField("client")
    field.setAccessible(true)
    val httpClient = field.get(jenkinsClient)
    val field2 = classOf[AbstractHttpClient].getDeclaredField("connManager")
    field2.setAccessible(true)
    field2.set(httpClient, cm)
    
    
    return jenkinsClient
  }

  @Provides
  def JenkinsServer(httpClient: JenkinsHttpClient): JenkinsServer = {
    new JenkinsServer(httpClient)
  }

}