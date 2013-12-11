#!/bin/sh

mvn install || exit 1
mvn assembly:single || exit 1
scp target/eng.dashboard-0.0.1-SNAPSHOT-jar-with-dependencies.jar delphix@eyal-eng-dash:/usr/local/eng.dashboard/eng-dashboard-0.1.jar || exit 1
ssh delphix@eyal-eng-dash 'killall java && GIT_DIR=/usr/local/dlpx-app-gate/.git nohup java -jar /usr/local/eng.dashboard/eng-dashboard-0.1.jar > eng-dash.log' || exit 1

