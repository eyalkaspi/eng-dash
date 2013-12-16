/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.app

import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.util.resource.Resource
import org.eclipse.jetty.util.resource.FileResource
import org.eclipse.jetty.util.resource.URLResource
import java.net.URL
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import com.google.common.io.ByteStreams
import java.io.InputStream

class StaticServlet extends HttpServlet {

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) = {
    val uri = if (req.getRequestURI() == "/") "/index.html" else req.getRequestURI()
    val classLoader = Thread.currentThread().getContextClassLoader()
    val input: InputStream = classLoader.getResourceAsStream("static" + uri)
    if (input != null) {
      ByteStreams.copy(input, resp.getOutputStream()); 
    } else {
    	super.doGet(req, resp);
    }
  }
}