@echo off
REM
REM Start an RSE Windows Server
REM Usage: server.bat [<port>] [<timeout>] [<clientUserID>]
REM

setlocal

set PORT=%1
set TIMEOUT=%2
set TICKET=%3
if xxx%1 == xxx set PORT=4033
if xxx%2 == xxx set TIMEOUT=120000

if "%1" == "?" goto usage
if "%1" == "/?" goto usage
if "%1" == "/h" goto usage
if "%1" == "help" goto usage
if "%1" == "/help" goto usage

IF NOT "%A_PLUGIN_PATH%"=="" GOTO doneSetup
IF EXIST setup.bat GOTO HaveSetup
ECHO.
ECHO Please run setup.bat before running server.bat
PAUSE
GOTO done
:HaveSetup
CALL setup.bat

:doneSetup
if xxx%3 == xxx goto runNoTicket
@echo on
java -DA_PLUGIN_PATH=%A_PLUGIN_PATH% -DDSTORE_SPIRIT_ON=true org.eclipse.dstore.core.server.Server %PORT% %TIMEOUT% %TICKET%
goto done

:runNoTicket
@echo on
java -DA_PLUGIN_PATH=%A_PLUGIN_PATH% -DDSTORE_SPIRIT_ON=true org.eclipse.dstore.core.server.Server %PORT% %TIMEOUT%
goto done

:usage
@echo Usage: server.bat ^<port^> ^<timeout^>  
pause

:done
endlocal