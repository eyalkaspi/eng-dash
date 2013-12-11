/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.app

import com.google.inject.servlet.GuiceServletContextListener
import com.google.inject.Injector

class ServletConfig(injector: Injector) extends GuiceServletContextListener {

  override def getInjector() = injector

}