package com.delphix.eng.dashboard.revision

object RevisionState extends Enumeration {
  type RevisionState = Value
  val INITIAL, RUNNING, COMPLETE = Value
}