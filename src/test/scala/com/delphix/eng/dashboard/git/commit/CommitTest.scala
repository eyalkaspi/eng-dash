/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.git.commit;

import org.junit.Test
import org.junit.Assert._
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.FunSuite

class CommitTest extends FunSuite {

	test("fromStringPositive") {
		val commit = Commit(
				List(
					"commit ae0032dee77f5be8b7b2ba3f2b5fd412c7e7e066",
					"Author: Eyal Kaspi <eyal.kaspi@delphix.com>", "Date: Wed Nov 27 17:41:32 2013 -0800", "",
					"29594 Return type specification inconsistent when not returning an OKResult"));
		expectResult(new CommitId("ae0032dee77f5be8b7b2ba3f2b5fd412c7e7e066"))(commit.id);
		expectResult(new Author("Eyal Kaspi <eyal.kaspi@delphix.com>"))(commit.author);
	}

	test("toStringPositive") {
		val commit = new Commit(
				new CommitId("ae0032dee77f5be8b7b2ba3f2b5fd412c7e7e066"),
				new Author("Eyal Kaspi <eyal.kaspi@delphix.com>"));
		expectResult("commit ae0032dee77f5be8b7b2ba3f2b5fd412c7e7e066\n" +
					"Author: Eyal Kaspi <eyal.kaspi@delphix.com>")(
					commit.toString());
					
	}
}
