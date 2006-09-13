#!/bin/bash

# exit codes of GameServer:
#  0 normal shutdown
#  2 reboot attempt

echo "© 2006 L2Jserver.com"
echo "L2J Game Server comes with ABSOLUTELY NO WARRANTY;"
echo "This is free software, and you are welcome to redistribute it under certain conditions;"
echo "see http://www.gnu.org/copyleft/gpl.html for more details."

while :; do
	[ -f log/java0.log.0 ] && mv log/java0.log.0 "log/`date +%Y-%m-%d_%H-%M-%S`_java.log"
	[ -f log/stdout.log ] && mv log/stdout.log "log/`date +%Y-%m-%d_%H-%M-%S`_stdout.log"
	java -Xms512m -Xmx512m -cp bsf.jar:javolution.jar:bsh-2.0.jar:jython.jar:c3p0-0.9.0.jar:mysql-connector-java-3.1.10-bin.jar:sqljdbc.jar:l2jserver.jar net.sf.l2j.gameserver.GameServer > log/stdout.log 2>&1
	[ $? -ne 2 ] && break
#	/etc/init.d/mysql restart
	sleep 10
done
