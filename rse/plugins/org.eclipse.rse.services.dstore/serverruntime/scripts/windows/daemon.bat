@ECHO OFF
REM
REM Start an RSE Windows Daemon
REM Usage: daemon.bat [<port> | <low port>-<high port>] [ <low server port>-<high server port>]
REM
SET DaemonPort=4035
SET ServerPortRange=
IF NOT "%1"=="" SET DaemonPort=%1
IF NOT "%2"=="" SET ServerPortRange=%2

if "%1" == "?" goto usage
if "%1" == "/?" goto usage
if "%1" == "/h" goto usage
if "%1" == "help" goto usage
if "%1" == "/help" goto usage

IF NOT "%A_PLUGIN_PATH%"=="" GOTO DoneSetup
IF EXIST setup.bat GOTO HaveSetup
ECHO.
ECHO Please run setup.bat before running daemon.bat
PAUSE
GOTO Done
:HaveSetup
CALL setup.bat
:DoneSetup
java -DA_PLUGIN_PATH=%A_PLUGIN_PATH% org.eclipse.dstore.core.server.ServerLauncher %DaemonPort% %ServerPortRange%
GOTO Done

:usage
@echo Usage: daemon.bat [^<port^> ^| ^<low port^>-^<high port^>] [^<low server port^>-^<high server port^>]
pause

:Done
