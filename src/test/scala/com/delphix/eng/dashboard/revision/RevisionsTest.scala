/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.revision

import scala.slick.jdbc.ResultSetInvoker
import org.scalatest.FunSuite
import com.delphix.eng.dashboard.git.commit.Author
import com.delphix.eng.dashboard.git.commit.CommitId
import com.delphix.eng.dashboard.git.commit.Commit
import com.delphix.eng.dashboard.persistence.TestDatabase
import com.delphix.eng.dashboard.git.commit.CommitId
import com.delphix.eng.dashboard.persistence.Id
import com.delphix.eng.dashboard.vm.VmIdentifier

class RevisionsTest extends FunSuite {

	test("insert") {
		val revisions = new Revisions(TestDatabase())
		revisions.createDDl
		revisions.save(CommitId("commit"), new VmIdentifier("vm1"), new Author("eyal@delphix.com"),
		    "xyz", RevisionType.PUSHING)
		val list = revisions.list
		expect(1)(list.size)
		val rev = Revision(Some(new Id[Revision](1)),
		    CommitId("commit"), RevisionState.COMPLETE, RevisionType.PUSHING, new VmIdentifier("vm1"),
		    new Author("eyal@delphix.com"), "xyz")
		expect(rev)(list.head)
	}
}
