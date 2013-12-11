package com.delphix.eng.dashboard.command

import org.scalatest.FunSuite

class LocalCommandExecutorTest extends FunSuite {

  test("echo") {
    val exec = new LocalCommandExecutorImpl()
    val lines = exec.execute(List("/bin/bash", "-c", "echo JOHN"))
    expectResult(List("JOHN"))(lines)
  }
  
   test("negative") {
    val exec = new LocalCommandExecutorImpl()
    intercept[RuntimeException] {
    	exec.execute(List("/bin/bash", "-c", "'exit 1'"))
    }
  }
}