@ECHO OFF
IF NOT "%A_PLUGIN_PATH%"=="" GOTO DoneSetup
IF EXIST setup.bat GOTO HaveSetup
ECHO.
ECHO Please run setup.bat before running daemon.bat
PAUSE
GOTO Done
:HaveSetup
CALL setup.bat
:DoneSetup
java -DA_PLUGIN_PATH=%A_PLUGIN_PATH% org.eclipse.dstore.core.server.ServerLauncher
:Done
