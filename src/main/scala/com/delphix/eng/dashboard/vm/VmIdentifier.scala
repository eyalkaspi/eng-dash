package com.delphix.eng.dashboard.vm;

class VmIdentifier(val id: String) extends AnyVal {
  def hostName = id + ".dcenter.delphix.com"
}
