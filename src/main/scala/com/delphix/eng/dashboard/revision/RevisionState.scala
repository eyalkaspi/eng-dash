/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.revision

object RevisionState extends Enumeration {
  type RevisionState = Value
  val INITIAL, RUNNING, COMPLETE, FAILED = Value
}