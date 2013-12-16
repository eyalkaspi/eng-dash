#!/bin/bash
#
# Copyright (c) 2013 by Delphix.
# All rights reserved.

function die
{
	echo "$(basename $0): $*"
	exit 1
}

# clean and compile (does not run tests yet)
mvn clean install || die "mvn install failed"

# assemble a single jar with all dependencies
mvn package || die "assembly the binary failed"

# scp the jar to the remote server
scp target/eng.dashboard-0.0.1-SNAPSHOT.jar\
	delphix@eyal-eng-dash.dcenter.delphix.com:/usr/local/eng.dashboard/eng-dashboard-0.1.jar || \
	die "could not scp jar"

# kill the server and restart it
ssh delphix@eyal-eng-dash.dcenter.delphix.com /bin/bash <<EOF
export PATH=/usr/bin:/bin:/usr/sbin

function die
{
	echo "failed: \$1" >&2
	echo "running as user '$user' on '$hostname'" >&2
	exit 1
}

killall java 2>/dev/null
(GIT_DIR=/usr/local/dlpx-app-gate/.git nohup java -jar \
	/usr/local/eng.dashboard/eng-dashboard-0.1.jar &>>eng-dash.log &)

echo "started java"
exit 0

EOF

