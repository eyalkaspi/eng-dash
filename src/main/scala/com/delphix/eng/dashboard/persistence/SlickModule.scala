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
    Class.forName("org.postgresql.Driver")
    bind(classOf[Database]).toInstance(Database.forURL(s"jdbc:postgresql://localhost:5432/delphix", driver = "org.postgresql.Driver"))
  }
  
}