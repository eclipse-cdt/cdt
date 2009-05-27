@echo off
@rem ***************************************************************************
@rem Copyright (c) 2005, 2006 IBM Corporation and others.
@rem All rights reserved. This program and the accompanying materials
@rem are made available under the terms of the Eclipse Public License v1.0
@rem which accompanies this distribution, and is available at
@rem http://www.eclipse.org/legal/epl-v10.html
@rem
@rem Contributors:
@rem IBM Corporation - initial API and implementation
@rem Martin Oberhuber (Wind River) - Add usage print, support run by dbl click
@rem ***************************************************************************
REM
REM Start an RSE Windows Server
REM Usage: server.bat [<port>] [<timeout>]
REM

setlocal

set PORT=%1
set TIMEOUT=%2
set TICKET=%3
if "%1" == "" set PORT=4033
if "%2" == "" set TIMEOUT=120000
REM set DSTORE_USER_PREFS=-Dclient.username=remoteuser

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
if "%3" == "" goto runNoTicket
REM The ticket parameter may be used internally by the daemon for starting a server
@echo on
java %DSTORE_USER_PREFS% -DA_PLUGIN_PATH=%A_PLUGIN_PATH% -DDSTORE_SPIRIT_ON=true org.eclipse.dstore.core.server.Server %PORT% %TIMEOUT% %TICKET%
@echo off
goto done

:runNoTicket
@echo on
java %DSTORE_USER_PREFS% -DA_PLUGIN_PATH=%A_PLUGIN_PATH% -DDSTORE_SPIRIT_ON=true org.eclipse.dstore.core.server.Server %PORT% %TIMEOUT%
@echo off
goto done

:usage
@echo Usage: server.bat [^<port^>] [^<timeout^>]
pause

:done
endlocal