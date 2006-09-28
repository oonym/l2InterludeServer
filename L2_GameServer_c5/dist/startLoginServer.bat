@echo off
:start
echo Starting L2J Login Server.
echo.
java -Xmx128m -cp javolution.jar;c3p0-0.9.0.jar;mysql-connector-java-3.1.10-bin.jar;l2jserver.jar; net.sf.l2j.loginserver.LoginServer
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
