/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.persistence

import scala.slick.session.Database
import scala.slick.driver.H2Driver.simple._
import TypeMappers._
import scala.slick.lifted.TypeMapper

abstract class RepoTable[M](name: String)(implicit tm: TypeMapper[Id[M]]) extends Table[M](name) {
  def id = column[Id[M]]("ID", O.PrimaryKey, O.AutoInc)
  def autoInc = * returning id
}