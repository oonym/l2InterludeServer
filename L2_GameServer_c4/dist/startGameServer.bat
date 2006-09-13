@echo off
:start
echo.
echo © 2006 L2Jserver.com
echo L2J Game Server comes with ABSOLUTELY NO WARRANTY;
echo This is free software, and you are welcome to redistribute it under certain conditions;
echo see http://www.gnu.org/copyleft/gpl.html for more details.
echo.
echo Starting L2J Game Server.
echo.
java -Xmx512m -cp bsf.jar;bsh-2.0.jar;javolution.jar;c3p0-0.9.0.jar;mysql-connector-java-3.1.10-bin.jar;sqljdbc.jar;l2jserver.jar;jython.jar net.sf.l2j.gameserver.GameServer
if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Admin Restart ...
echo.
goto start
:error
echo.
echo Server terminated abnormaly
echo.
:end
echo.
echo server terminated
echo.
pause
