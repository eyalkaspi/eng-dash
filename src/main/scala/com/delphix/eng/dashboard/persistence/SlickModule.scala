/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.persistence

import com.google.inject.AbstractModule
import scala.slick.session.Database

class SlickModule extends AbstractModule {
  
  def configure() {
    val GIT_DIR = System.getenv("GIT_DIR")
    bind(classOf[Database]).toInstance(Database.forURL(s"jdbc:h2:${GIT_DIR}/h2.db", driver = "org.h2.Driver"))
  }
  
}