/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.time

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

import com.google.inject.ImplementedBy

@ImplementedBy(classOf[SleepyImpl])
trait Sleepy {
  def sleep(time: Long, unit: TimeUnit = TimeUnit.MILLISECONDS)
  def retry[T](count: Int, period: (Long, TimeUnit))(task: () => T): T
}

class SleepyImpl extends Sleepy {

  def sleep(time: Long, unit: TimeUnit = TimeUnit.MILLISECONDS) = {
    unit.sleep(time)
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