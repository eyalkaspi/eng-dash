/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.vm;

class VmIdentifier(val id: String) {
  def hostName = id + ".dcenter.delphix.com"
}
