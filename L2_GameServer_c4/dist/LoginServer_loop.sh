#!/bin/bash

echo "© 2006 L2Jserver.com"
echo "L2J Game Server comes with ABSOLUTELY NO WARRANTY;"
echo "This is free software, and you are welcome to redistribute it under certain conditions;"
echo "see http://www.gnu.org/copyleft/gpl.html for more details."

err=1
until [ $err == 0 ]; 
do
	mv log/java0.log.0 "log/`date +%Y-%m-%d_%H-%M-%S`_java.log"
	mv log/stdout.log "log/`date +%Y-%m-%d_%H-%M-%S`_stdout.log"
	nice -n -2 java -Xms512m -Xmx512m -cp javolution.jar:c3p0-0.9.0.jar:mysql-connector-java-3.1.10-bin.jar:sqljdbc.jar:l2jserver.jar net.sf.l2j.loginserver.LoginServer > log/stdout.log 2>&1
	err=$?
#	/etc/init.d/mysql restart
	sleep 10;
done
