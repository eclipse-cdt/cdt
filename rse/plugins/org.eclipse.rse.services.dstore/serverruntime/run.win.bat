echo off

setlocal

set PORT=%1
set TIMEOUT=%2
set TICKET=%3

if xxx%1 == xxx set PORT=4033
if xxx%2 == xxx set TIMEOUT=120000
if xxx%3 == xxx goto runNoTicket

@echo on

java -DA_PLUGIN_PATH=%A_PLUGIN_PATH% com.ibm.etools.systems.dstore.core.server.Server %PORT% %TIMEOUT% %TICKET%
goto done

:runNoTicket
@echo on
java -DA_PLUGIN_PATH=%A_PLUGIN_PATH% com.ibm.etools.systems.dstore.core.server.Server %PORT% %TIMEOUT%
goto done

:usage
@echo Usage: run.win ^<port^> ^<timeout^>  

:done
endlocal