@echo off
:start
echo Starting L2J Game Server.
echo.
REM -------------------------------------
REM Default parameters for a basic server.
java -Xmx512m -cp bsf.jar;bsh-2.0b4.jar;commons-logging-1.1.jar;mmocore.jar;javolution.jar;c3p0-0.9.1.1.jar;mysql-connector-java-5.0.5-bin.jar;l2jserver.jar;jython.jar net.sf.l2j.gameserver.GameServer
REM
REM If you have a big server and lots of memory, you could experiment for example with
REM java -server -Xmx1536m -Xms1024m -Xmn512m -XX:PermSize=256m -XX:SurvivorRatio=8 -Xnoclassgc -XX:+AggressiveOpts
REM -------------------------------------
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
