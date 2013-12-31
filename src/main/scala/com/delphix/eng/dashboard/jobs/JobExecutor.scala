/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.jobs;

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import com.sun.jersey.spi.inject.Inject
import com.delphix.eng.dashboard.git.commit.CommitId
import java.util.concurrent.Callable

class JobExecutor {

  var executorService = Executors.newCachedThreadPool()

  def schedule[T](job: () => T): Future[T] = {
    return executorService.submit(new Callable[T]() {

      override def call(): T = {
        return job()
      }
    });
  }
}
