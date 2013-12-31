/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.revision

import com.delphix.eng.dashboard.git.commit.CommitId
import com.delphix.eng.dashboard.persistence.Id
import RevisionState._
import RevisionType._
import com.delphix.eng.dashboard.vm.VmIdentifier
import com.delphix.eng.dashboard.git.commit.Author

case class Revision(id: Option[Id[Revision]], commitId: CommitId, state: RevisionState,
    revisionType: RevisionType, vm: VmIdentifier, author: Author, commitMsg: String)
