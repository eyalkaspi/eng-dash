/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.jenkins

import com.delphix.eng.dashboard.git.commit.CommitId
import com.delphix.eng.dashboard.persistence.Id
import com.offbytwo.jenkins.model.Build
import com.delphix.eng.dashboard.persistence.Id
import com.delphix.eng.dashboard.revision.Revision
import JenkinsJobState._
import JenkinsJobType._

case class JenkinsJob(
    id: Option[Id[JenkinsJob]],
    url: Option[String],
    state: Option[JenkinsJobState],
    jobType: JenkinsJobType,
    revision: Id[Revision])

