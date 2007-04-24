#!/bin/bash

# exit codes of GameServer:
#  0 normal shutdown
#  2 reboot attempt

while :; do
	[ -f log/java0.log.0 ] && mv log/java0.log.0 "log/`date +%Y-%m-%d_%H-%M-%S`_java.log"
	[ -f log/stdout.log ] && mv log/stdout.log "log/`date +%Y-%m-%d_%H-%M-%S`_stdout.log"
	java -Xms512m -Xmx512m -cp bsf.jar:bsh-2.0b4.jar:commons-logging-1.1.jar:javolution.jar:mmocore.jar:jython.jar:c3p0-0.9.1.jar:mysql-connector-java-5.0.5-bin.jar:l2jserver.jar net.sf.l2j.gameserver.GameServer > log/stdout.log 2>&1
	[ $? -ne 2 ] && break
#	/etc/init.d/mysql restart
	sleep 10
done
