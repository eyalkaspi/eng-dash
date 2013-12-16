/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.jenkins

object JenkinsJobType extends Enumeration {
  type JenkinsJobType = Value
  val PRECOMMIT, BLACKBOX = Value
  
  class JobTypeValue(jobType: Value) {
      def name() = jobType match {
        case PRECOMMIT => "app-precommit"
        case BLACKBOX => "eyal-eng-dash"
      }
   }

   implicit def value2StateValue(state: Value) = new JobTypeValue(state)
}