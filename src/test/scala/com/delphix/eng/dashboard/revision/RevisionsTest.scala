package com.delphix.eng.dashboard.revision

import org.junit.runner.RunWith
import scala.slick.jdbc.ResultSetInvoker
import org.scalatest.FunSuite
import com.delphix.eng.dashboard.git.commit.Author
import com.delphix.eng.dashboard.git.commit.CommitId
import com.delphix.eng.dashboard.git.commit.Commit
import com.delphix.eng.dashboard.persistence.TestDatabase
import com.delphix.eng.dashboard.git.commit.CommitId
import com.delphix.eng.dashboard.persistence.Id

class RevisionsTest extends FunSuite {

	test("insert") {
		val revisions = new Revisions(TestDatabase())
		revisions.createDDl
		revisions.save(CommitId("commit"))
		val list = revisions.list
		expect(1)(list.size)
		val rev = Revision(Some(new Id[Revision](1)), CommitId("commit"), RevisionState.COMPLETE)
		expect(rev)(list.head)
	}
}
