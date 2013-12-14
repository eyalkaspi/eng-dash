/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.git.commit;

import java.util.List;

import com.google.common.base.Joiner;

class Commit(val id: CommitId, val author: Author) {
  override def toString() = s"commit ${id.id}\nAuthor: ${author.email}"
}
