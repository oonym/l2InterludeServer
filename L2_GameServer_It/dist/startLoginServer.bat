@echo off
title Login Server Console
:start
echo Starting L2J Login Server.
echo.
java -Xmx128m -cp javolution.jar;mmocore.jar;c3p0-0.9.1.2.jar;mysql-connector-java-5.0.7-bin.jar;l2jserver.jar net.sf.l2j.loginserver.L2LoginServer
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
