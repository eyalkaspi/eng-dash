/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.jenkins

import java.util.UUID
import scala.collection.JavaConversions._
import org.apache.http.client.HttpResponseException
import com.delphix.eng.dashboard.persistence.Id
import com.delphix.eng.dashboard.revision.Revision
import com.delphix.eng.dashboard.time.Sleepy
import com.delphix.eng.dashboard.vm.VmIdentifier
import com.google.inject.Inject
import com.offbytwo.jenkins.JenkinsServer
import com.offbytwo.jenkins.client.JenkinsHttpClient
import com.offbytwo.jenkins.model.Build
import com.offbytwo.jenkins.model.BuildResult
import com.offbytwo.jenkins.model.BuildWithDetails
import com.offbytwo.jenkins.model.JobWithDetails
import java.util.concurrent.TimeUnit
import JenkinsJobState._
import JenkinsJobType._
import scala.collection.mutable.MutableList

class JenkinsClient @Inject() (
  val server: JenkinsServer,
  val jenkinsJobs: JenkinsJobs,
  val httpClient: JenkinsHttpClient,
  val sleepy: Sleepy) {

  def getJobDetails(job: JenkinsJob): BuildWithDetails = {
    val build = new Build(job.id.get.id, job.url.get)
    build.setClient(httpClient)
    build.details()
  }

  def suspend(revision: Revision) = {
    runJob(JenkinsJobType.DC_SUSPEND, revision.id.get,
      Map(
        "VM_NAMES" -> revision.vm.id,
        "EXPIRES" -> "1",
        "DCENTER_HOST" -> "dcenter"))
  }

  def run(revision: Revision, jobTypes: Seq[JenkinsJobType]): Map[Id[JenkinsJob], JenkinsJobState] = {
    monitorAndWait(jobTypes.map { run(revision, _) })
  }

  def dxPush(revision: Revision, branch: String) = {
    monitorAndWait(List(runJob(JenkinsJobType.DX_PUSH, revision.id.get,
      Map("BRANCH" -> branch,
          "EMAIL" -> revision.author.email))))
  }

  private def run(revision: Revision, jobType: JenkinsJobType): Id[JenkinsJob] = {
    jobType match {
      case JenkinsJobType.PRECOMMIT => runPrecommit(revision)
      case JenkinsJobType.BLACKBOX => runBlackBox(revision)
      case JenkinsJobType.DC_SUSPEND => suspend(revision)
    }
  }

  def runPrecommit(revision: Revision) = {
    runJob(JenkinsJobType.PRECOMMIT, revision.id.get,
      Map(
        "VM_NAME" -> revision.vm.id,
        "DCENTER_HOST" -> "dcenter",
        "APP_GATE_PATH" -> "/export/home/delphix/dlpx-app-gate",
        "EMAIL" -> revision.author.email))
  }

  def runBlackBox(revision: Revision) = {
    runJob(JenkinsJobType.BLACKBOX, revision.id.get,
      Map(
        "VM_NAME" -> revision.vm.id,
        "EMAIL" -> revision.author.email))
  }

  private def runJob(jobType: JenkinsJobType, revision: Id[Revision], parameters: Map[String, String]): Id[JenkinsJob] = {
    val jenkinsJob = server.getJob(jobType.name)
    var lastBuildNumber = jenkinsJob.details().getLastBuild().getNumber()

    val uuid = UUID.randomUUID().toString()
    launchJob(jenkinsJob, parameters + ("BUILD_UUID" -> uuid))

    // see https://issues.jenkins-ci.org/browse/JENKINS-12827, the API does not return the build number
    var newJob: Build = null
    var retryCount = 0;
    var sleep = 1000;
    while (newJob == null && retryCount < 5) {
      val job = jenkinsJob.details()
      if (job.getLastBuild().getNumber() == lastBuildNumber) {
        Thread.sleep(200)
      } else {
        val maybeJob = job.getBuilds().find { j =>
          uuid.equals(j.details().getParameters().get("BUILD_UUID"))
        } match {
          case Some(x) => newJob = x;
          case None => {
            println("new job not found yet")
            Thread.sleep(sleep)
            sleep = sleep * 2;
            retryCount = retryCount + 1
          }
        }
      }
    }
    return jenkinsJobs.listByRevision(revision) find { _.jobType == jobType } match {
      case Some(j) =>
        val jobId = j.id.get
        jenkinsJobs.updateUrl(jobId, newJob.getUrl())
        jobId
      case None =>
	    jenkinsJobs.save(Some(newJob.getUrl()), None, jobType, revision)
    }
  }

  private def launchJob(jenkinsJob: JobWithDetails, parameters: Map[String, String]) = {
    try {
      // For some reason that I ignore, jenkins sends a redirect to itself in a loop
      jenkinsJob.build(parameters)
    } catch {
      case e: HttpResponseException => if (e.getStatusCode() != 302) {
        throw e
      }
    }
  }

  def monitorAndWait(jobIds: Seq[Id[JenkinsJob]]): Map[Id[JenkinsJob], JenkinsJobState] = {
    // val jobId = job.id
    var remainingJobs = jobIds
    var jobResults = Map[Id[JenkinsJob], JenkinsJobState]()
    while (!remainingJobs.isEmpty) {
      remainingJobs.foreach { jobId =>
        try {
          val job = jenkinsJobs.get(jobId)
          val details = getJobDetails(job)
          if (details.getResult() != null) {
            val jobState = JenkinsJobState withName (details.getResult().name())
            if (jobState != job.state) {
              jenkinsJobs.updateState(jobId, jobState)
              jobResults = jobResults + (jobId -> jobState)
            }
          }
          if (details.isBuilding()) {
            println(s"r${job.revision.id}:${job.jobType}[${job.id}] => building")
          } else {
            println(s"r${job.revision.id}:${job.jobType}[${job.id}] => ${details.result}")
            remainingJobs = remainingJobs.filter {_ != jobId}
          }
        } catch {
          case e: Exception => e.printStackTrace()
        }
        sleepy.sleep(10, TimeUnit.SECONDS)
      }
    }
    jobResults
  }
}