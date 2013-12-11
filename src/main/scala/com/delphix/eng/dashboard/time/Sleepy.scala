package com.delphix.eng.dashboard.time

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

import com.google.inject.ImplementedBy

/**
 * Utility class to tests to control sleep statements
 */
@ImplementedBy(classOf[SleepyImpl])
trait Sleepy {
  def sleep(time: Long, unit: TimeUnit = TimeUnit.MILLISECONDS)
  def runAtFixedInterval(period: Long, unit: TimeUnit, task: (ScheduledFuture[_]) => Any)
  def retry[T](count: Int, period: (Long, TimeUnit))(task: () => T): T
}

class SleepyImpl extends Sleepy {

  val executor = Executors.newScheduledThreadPool(32)

  def sleep(time: Long, unit: TimeUnit = TimeUnit.MILLISECONDS) = {
    unit.sleep(time)
  }

  def runAtFixedInterval(period: Long, unit: TimeUnit, task: (ScheduledFuture[_]) => Any) = {
    var future: ScheduledFuture[_] = null
    future = executor.scheduleAtFixedRate(new Runnable() {
      def run(): Unit = task(future)
    }, 0, period, unit)
  }

  def retry[T](count: Int, period: (Long, TimeUnit))(task: () => T): T = {
    try {
      return task()
    } catch {
      case e: Exception => {
        if (count == 0) {
          throw e
        }
        sleep(period._1, period._2)
        return retry(count - 1, period)(task)
      }
    }
  }
}