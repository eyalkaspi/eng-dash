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
		// TODO: persist job
		System.out.println("scheduling " + job.getClass().getSimpleName());
		return executorService.submit(new Callable[Any]() {
			
			override def call(): Any = {
				System.out.println("Running " + job.getClass().getSimpleName());
				// TODO: persist
				var success = false;
				try {
					job()
					success = true;
				} catch {
				  case e: RuntimeException => {
						if (e.getCause().isInstanceOf[InterruptedException]) {
							return;
						}
					  }
					e.printStackTrace();
				} finally {
					//job.setState(success ? JobState.COMPLETED : JobState.FAILED);
					// TODO: persist
					System.out.println("Completed " + job.getClass().getSimpleName());
				}
			}
		});
	}
}
