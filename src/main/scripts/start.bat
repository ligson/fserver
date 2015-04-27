@echo off
set STORE_SERVER_HOME=%~dp0..
set LOCALCLASSPATH=%STORE_SERVER_HOME%\.;%STORE_SERVER_HOME%\conf
for %%f in (%STORE_SERVER_HOME%\lib\*.jar) do call %STORE_SERVER_HOME%\bin\fserver %%f
for %%f in (%STORE_SERVER_HOME%\conf\*.*) do call %STORE_SERVER_HOME%\bin\fserver %%f

:execute
echo java -classpath %LOCALCLASSPATH% %1 %2 %3 %4 %5 %6 %7 %8 %9
java -classpath %LOCALCLASSPATH% com.boful.net.fserver.FServer

pause