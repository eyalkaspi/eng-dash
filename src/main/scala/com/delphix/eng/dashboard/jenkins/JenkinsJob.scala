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
    id: Id[JenkinsJob],
    url: String,
    state: JenkinsJobState,
    jobType: JenkinsJobType,
    revision: Id[Revision])

