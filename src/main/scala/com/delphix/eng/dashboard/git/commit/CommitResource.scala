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
import com.delphix.eng.dashboard.jenkins.JenkinsJobType._
import scala.collection.mutable.MutableList
import javax.ws.rs.core.Response
import javax.ws.rs.core.StreamingOutput
import java.net.InetAddress
import com.google.inject.name.Named
import com.delphix.eng.dashboard.jenkins.JenkinsJobState
import com.delphix.eng.dashboard.jenkins.JenkinsJobType
import javax.ws.rs.core.PathSegment
import javax.ws.rs.QueryParam
import scala.collection.JavaConversions._

@Path("/git")
class CommitResource @Inject() (
  val handler: GitCommitHandler,
  val revisions: Revisions,
  val jenkinsJobs: JenkinsJobs,
  @Named("homepage") homepage: String) {

  @POST
  @Consumes(Array("text/plain"))
  @Path("/{id}")
  def postPush(@PathParam("id") id: String, @QueryParam("branches") branches: java.util.List[String]): String = {
    try {
      val jobTypes = branches.toList.map { b =>
        b match {
          case "refs/heads/precommit" => JenkinsJobType.PRECOMMIT
          case "refs/heads/dx-push" => JenkinsJobType.PRECOMMIT
          case "refs/heads/blackbox-normal" => JenkinsJobType.BLACKBOX
          case _ => throw new IllegalArgumentException(
            "Branch must be one of [precommit,blackbox-normal,dx-push]. Examples\n\n" +
              "   # run precommit only\n" +
              "   git push eng.dash <branch>:precommit\n\n" +
              "   # run blackbox only\n" +
              "   git push eng.dash <branch>:blackbox-normal\n\n" +
              "   # run precommit and blackbox\n" +
              "   git push eng.dash <branch>:precommit <branch>:blackbox-normal")
        }
      }
      var pushUpstream = branches.toList.contains("refs/heads/dx-push")
      handle(new CommitId(id), jobTypes, pushUpstream)
    } catch {
      case e: IllegalArgumentException =>
        return s"\n\n\n-----------------\n\n\n${e.getMessage()}\n\n\n-----------------\n"
    }
  }

  private def handle(commit: CommitId, jobTypes: Seq[JenkinsJobType], pushUpstream: Boolean): String = {
    val msg: MutableList[String] = MutableList.empty
    msg += "\n\n\n-----------------"
    revisions.getByCommitId(commit) filter { _.state == RevisionState.INITIAL } match {
      case Some(r) =>
        msg += s"Your revision ${commit.id} is currently being processed\n"
      case _ =>
        handler.handleNewCommit(commit, jobTypes, pushUpstream)
        msg += s"Running ${jobTypes.mkString(",")} on ${commit.id}"
    }
    msg += s"\nVisit ${homepage}#${commit.id} for the status\n\n"
    msg += "PLEASE IGNORE THE [remote rejected] MESSAGE BELOW"
    msg += "-----------------\n\n\n"
    return msg.mkString("\n")
  }
}