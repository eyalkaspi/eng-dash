/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.persistence

import scala.slick.lifted.BaseTypeMapper
import scala.slick.lifted.MappedTypeMapper
import com.delphix.eng.dashboard.git.commit.CommitId
import com.delphix.eng.dashboard.revision.Revision
import scala.slick.lifted.TypeMapper
import com.delphix.eng.dashboard.jenkins.JenkinsJob

object TypeMappers {
  implicit val commitIdType: MappedTypeMapper[CommitId, String] = MappedTypeMapper.base[CommitId, String](
    { commitId => commitId.id },
    { id => new CommitId(id) }).asInstanceOf[MappedTypeMapper[CommitId, String]]

  implicit val jenkinsJobTypeMapper = TypeMappers.idType[JenkinsJob]()
  implicit val revisionTypeMapper = TypeMappers.idType[Revision]()

  
  def idType[M](): BaseTypeMapper[Id[M]] = {
    MappedTypeMapper.base[Id[M], Int](
      { id => id.id },
      { id => Id[M](id)})
  }
}