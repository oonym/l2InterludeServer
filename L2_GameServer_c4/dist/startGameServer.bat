@echo off
:start
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
