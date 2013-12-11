package com.delphix.eng.dashboard.git.commit

import com.google.inject.Inject
import javax.ws.rs.POST
import javax.ws.rs.Consumes
import javax.ws.rs.PathParam
import javax.ws.rs.Path
import javax.ws.rs.core.MediaType

@Path("/git")
class CommitResource {

  @Inject val handler: GitCommitHandler = null

  @POST
  @Consumes(Array("text/plain"))
  @Path("/commit/{id}")
  def postPush(@PathParam("id") id: String) = handler.handleNewCommit(new CommitId(id))
}