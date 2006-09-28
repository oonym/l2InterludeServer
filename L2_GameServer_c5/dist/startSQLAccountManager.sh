#!/bin/sh
java -Djava.util.logging.config.file=console.cfg -cp c3p0-0.9.0.jar:l2jserver.jar:mysql-connector-java-3.1.10-bin.jar net.sf.l2j.accountmanager.SQLAccountManager
