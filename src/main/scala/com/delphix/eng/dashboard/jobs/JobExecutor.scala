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
	
	val executorService = Executors.newCachedThreadPool()
	
	def schedule(job: () => Unit, commitId: CommitId): Future[Any] = {
		return executorService.submit(new Callable[Any]() {
			
			override def call(): Any = {
				var success = false;
				try {
					job()
					success = true;
				} catch {
				  case e: Throwable => {
						if (e.getCause().isInstanceOf[InterruptedException]) {
							return;
						}
					  }
					e.printStackTrace();
				}
			}
		});
	}
}
