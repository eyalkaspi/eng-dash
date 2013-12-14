/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.jenkins

object JenkinsJobState extends Enumeration {
  type JenkinsJobState = Value
  val FAILURE, UNSTABLE, REBUILDING, BUILDING, ABORTED, SUCCESS, UNKNOWN = Value
  
  class StateValue(state: Value) {
      def isBuilding() = state match {
        case BUILDING | UNKNOWN | REBUILDING => true
        case _ => false
      }
   }

   implicit def value2StateValue(state: Value) = new StateValue(state)
}