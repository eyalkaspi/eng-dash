/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.git.commit

import com.google.inject.Inject
import javax.ws.rs.POST
import javax.ws.rs.Consumes
import javax.ws.rs.PathParam
import javax.ws.rs.Path
import javax.ws.rs.core.MediaType
import com.delphix.eng.dashboard.revision.Revisions
import com.delphix.eng.dashboard.revision.Revision
import com.delphix.eng.dashboard.revision.RevisionState
import com.delphix.eng.dashboard.jenkins.JenkinsJobs
import scala.collection.mutable.MutableList
import javax.ws.rs.core.Response
import javax.ws.rs.core.StreamingOutput

@Path("/git")
class CommitResource @Inject() (
  val handler: GitCommitHandler,
  val revisions: Revisions,
  val jenkinsJobs: JenkinsJobs) {

  @POST
  @Consumes(Array("text/plain"))
  @Path("/commit/{id}")
  def postPush(@PathParam("id") id: String): String = {
    val commit = new CommitId(id)
    val msg: MutableList[String] = MutableList.empty
    msg += "\n\n\n-----------------"
    revisions.getByCommitId(commit) match {
      case Some(r) =>
        r.state match {
          case RevisionState.INITIAL =>
            msg += s"Your revision ${id} is currently being processed\n"
            msg += s"Try again later to get the jenkins job urls\n"
          case RevisionState.FAILED =>
            handler.retryCommit(commit, r)
            msg += s"Your revision ${id} has failed do to an internal error\n"
            msg += s"Retrying....\n"
          case _ =>
            val jobs = (for (j <- jenkinsJobs.listByRevision(r.id.get)) yield j)
            
            r.state match {
              case RevisionState.RUNNING =>
                msg += s"Your revision ${id} is currently building"
                if (jobs.isEmpty) {
                	msg += s"Try again later to get the jenkins job urls\n"
                }
              case RevisionState.COMPLETE =>
                msg += s"Your revision ${id} is complete"
            }
            jobs.foreach { j =>
              msg += s"${j.url} => ${j.state}"
            }
        }
      case _ =>
        handler.handleNewCommit(commit)
        msg += s"Running precommit and blackbox on ${id}"
    }
    msg += "PLEASE IGNORE THE [remote rejected] MESSAGE BELOW"
    msg += "-----------------\n\n\n"
    return msg.mkString("\n")
  }
}