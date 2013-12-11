/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.jenkins

object JenkinsJobState extends Enumeration {
  type JenkinsJobState = Value
  val FAILURE, UNSTABLE, REBUILDING, BUILDING, ABORTED, SUCCESS, UNKNOWN = Value
}