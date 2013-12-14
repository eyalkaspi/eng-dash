/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.persistence

import scala.slick.lifted.MappedTypeMapper
import scala.slick.lifted.TypeMapper

import org.scalatest.FunSuite

import com.delphix.eng.dashboard.git.commit.CommitId
import com.delphix.eng.dashboard.revision.Revision

class TypeMappersTest extends FunSuite {

  test("commitIdType") {
    val typeMapper = TypeMappers.commitIdType.asInstanceOf[MappedTypeMapper[CommitId, String]]
    expect("hello") (typeMapper.map(CommitId("hello")))
    expect(CommitId("hello")) (typeMapper.comap("hello"))
  }
  
  test("idType") {
    val typeMapper = TypeMappers.idType[Revision].asInstanceOf[MappedTypeMapper[Id[Revision], Int]]
    expect(123) (typeMapper.map(Id[Revision](123)))
    expect(Id[Revision](123)) (typeMapper.comap(123))
  }
}