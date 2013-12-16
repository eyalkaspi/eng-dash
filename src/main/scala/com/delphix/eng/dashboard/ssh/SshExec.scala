/**
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
package com.delphix.eng.dashboard.ssh

import net.schmizz.sshj.connection.channel.direct.Session.Command

trait SshExec {
	def exec(command: String*): String
	def exec(exceptionHandler: (Int, String, String) => Unit, command: String*): String 
}