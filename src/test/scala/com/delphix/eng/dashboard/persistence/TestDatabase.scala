package com.delphix.eng.dashboard.persistence

import scala.slick.session.Database

object TestDatabase {
	def apply() = Database.forURL("jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
}