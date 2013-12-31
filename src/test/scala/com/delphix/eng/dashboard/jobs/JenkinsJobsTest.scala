package com.delphix.eng.dashboard.jobs

import org.scalatest.FunSuite
import com.delphix.eng.dashboard.git.commit.Author
import com.delphix.eng.dashboard.git.commit.Commit
import com.delphix.eng.dashboard.git.commit.CommitId
import com.delphix.eng.dashboard.git.commit.CommitId
import com.delphix.eng.dashboard.jenkins.JenkinsJobs
import com.delphix.eng.dashboard.persistence.Id
import com.delphix.eng.dashboard.persistence.TestDatabase
import com.delphix.eng.dashboard.revision.Revisions
import com.delphix.eng.dashboard.vm.VmIdentifier
import com.delphix.eng.dashboard.jenkins.JenkinsJob
import com.delphix.eng.dashboard.jenkins.JenkinsJobState
import com.delphix.eng.dashboard.jenkins.JenkinsJobType
import com.delphix.eng.dashboard.revision.RevisionType

class JenkinsJobsTest extends FunSuite {

  test("deleteByRevision") {
	  val db = TestDatabase()
		val revisions = new Revisions(db)
		revisions.createDDl
		val r1 = revisions.save(CommitId("commit"), new VmIdentifier("vm1"), 
		    new Author("eyal@delphix.com"), "xyz", RevisionType.PUSHING)
		val r2 = revisions.save(CommitId("commit2"), new VmIdentifier("vm1"), 
		    new Author("eyal@delphix.com"), "xyz", RevisionType.PUSHING)
		
		val jobs = new JenkinsJobs(db, revisions)
		jobs.createDDl
		jobs.save(Some("url"), None, JenkinsJobType.PRECOMMIT, r1)
		jobs.save(Some("url"), None, JenkinsJobType.PRECOMMIT, r1)
		jobs.save(Some("url"), None, JenkinsJobType.PRECOMMIT, r2)

		jobs.deleteForRevision(r2)
		assert(jobs.list.size == 2)
		
		jobs.deleteForRevision(r1)
		assert(jobs.list.size == 0)
	}
  
}
