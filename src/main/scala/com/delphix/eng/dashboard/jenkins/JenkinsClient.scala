/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.jenkins

import scala.collection.JavaConversions._
import org.apache.http.client.HttpResponseException
import com.delphix.eng.dashboard.vm.VmIdentifier
import com.offbytwo.jenkins.JenkinsServer
import com.offbytwo.jenkins.model.Build
import com.delphix.eng.dashboard.persistence.Id
import java.util.UUID
import com.offbytwo.jenkins.client.JenkinsHttpClient
import com.offbytwo.jenkins.model.BuildResult
import com.offbytwo.jenkins.model.BuildWithDetails
import com.delphix.eng.dashboard.revision.Revision
import com.delphix.eng.dashboard.vm.VmIdentifier

class JenkinsClient(val server: JenkinsServer, val httpClient: JenkinsHttpClient) {

  def getJobDetails(job: JenkinsJob): BuildWithDetails = {
    val build = new Build(job.id.id, job.url)
    build.setClient(httpClient)
    build.details()
  }

  def suspend(vm: VmIdentifier) = {
    // For some reason that I ignore, jenkins sends a redirect to itself in a loop
    try {
      val uuid = UUID.randomUUID().toString()
      server.getJob("dcenter-suspend")
        .build(Map(
          "VM_NAMES" -> vm.id,
          "EXPIRES" -> "1",
          "DCENTER_HOST" -> "dcenter",
          "BUILD_UUID" -> uuid))
    } catch {
      case e: HttpResponseException => if (e.getStatusCode() != 302) {
        throw e
      }
    }
    // TODO: monitor the suspend job and try again in case of failures    	
  }

  def runPrecommit(vm: VmIdentifier, revision: Id[Revision]): JenkinsJob = {
    val precommitJob = server.getJob("app-precommit")
    var lastBuildNumber = precommitJob.details().getLastBuild().getNumber()
    val uuid = UUID.randomUUID().toString()

    // For some reason that I ignore, jenkins sends a redirect to itself in a loop
    try {
      precommitJob.build(Map(
        "VM_NAME" -> vm.id,
        "DCENTER_HOST" -> "dcenter",
        "APP_GATE_PATH" -> "/export/home/delphix/dlpx-app-gate",
        "EMAIL" -> "eyal@delphix.com", // TODO: use the author of the commit
        "BUILD_UUID" -> uuid))
    } catch {
      case e: HttpResponseException => if (e.getStatusCode() != 302) {
        throw e
      }
    }
    // see https://issues.jenkins-ci.org/browse/JENKINS-12827, the API does not return the build number
    var newJob: Build = null
    while (newJob == null) {
      val job = precommitJob.details()
      if (job.getLastBuild().getNumber() == lastBuildNumber) {
        Thread.sleep(200)
      } else {
        val maybeJob = job.getBuilds().find { j =>
          uuid.equals(j.details().getParameters().get("BUILD_UUID"))
        } match {
          case Some(x) => newJob = x;
          case None => {
            println("new job not found yet")
            Thread.sleep(200)
          }
        }
      }
    }
    return JenkinsJob(Id(newJob.getNumber()), newJob.getUrl(),
      JenkinsJobState.UNKNOWN, revision)
  }
}