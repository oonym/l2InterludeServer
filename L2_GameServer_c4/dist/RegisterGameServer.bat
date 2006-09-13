echo .
echo.
echo © 2006 L2Jserver.com
echo L2J Game Server comes with ABSOLUTELY NO WARRANTY;
echo This is free software, and you are welcome to redistribute it under certain conditions;
echo see http://www.gnu.org/copyleft/gpl.html for more details.
echo.
echo Registering Gameserver
@java -Djava.util.logging.config.file=console.cfg -cp c3p0-0.9.0.jar;l2jserver.jar;mysql-connector-java-3.1.10-bin.jar;sqljdbc.jar net.sf.l2j.gsregistering.GameServerRegister
@pause