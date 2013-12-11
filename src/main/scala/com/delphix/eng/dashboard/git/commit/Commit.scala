/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.git.commit;

import java.util.List;

import com.google.common.base.Joiner;

class Commit(val id: CommitId, val author: Author) {
  override def toString() = s"commit ${id.id}\nAuthor: ${author.name}"
}

object Commit {
  def apply(lines: Seq[String]) = {
    require(lines.size == 5, s"Wrong commit format ${lines.mkString("\n")}")
    val id = readCommitId(lines(0))
    val author = readAuthor(lines(1))
    new Commit(id, author);
  }

  def readAuthor(line: String) = {
    val s = line.split(" ")
    require(s.length > 1, s"Wrong author format ${line}. Expected [Author: <Author>]")
    require(s(0) == "Author:", s"Wrong author format ${line}. Expected [Author: <Author>]")
    new Author(s.filter(e => e != "Author:").mkString(" "))
  }

  def readCommitId(line: String) = {
    val s = line.split(" ")
    require(s.length == 2, s"Wrong commit Id format ${line}. Expected [commit <id>]")
    require(s(0) == "commit", s"Wrong commit Id format ${line}. Expected [commit <id>]")
    new CommitId(s(1))
  }
}
