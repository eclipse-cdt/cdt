@ECHO OFF
@rem ***************************************************************************
@rem Copyright (c) 2005, 2008 IBM Corporation and others.
@rem All rights reserved. This program and the accompanying materials
@rem are made available under the terms of the Eclipse Public License v1.0
@rem which accompanies this distribution, and is available at
@rem http://www.eclipse.org/legal/epl-v10.html
@rem
@rem Contributors:
@rem IBM Corporation - initial API and implementation
@rem Martin Oberhuber (Wind River) - Bug 142952: support run by dbl click
@rem Martin Oberhuber (Wind River) - Add usage print, set variables local
@rem ***************************************************************************
REM
REM Start an RSE Windows Daemon
REM Usage: daemon.bat [<port> | <low port>-<high port>] [ <low server port>-<high server port>]
REM
setlocal

SET DaemonPort=4075
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
@echo on
java -DA_PLUGIN_PATH=%A_PLUGIN_PATH% org.eclipse.dstore.core.server.ServerLauncher %DaemonPort% %ServerPortRange%
@echo off
GOTO Done

:usage
@echo Usage: daemon.bat [^<port^> ^| ^<low port^>-^<high port^>] [^<low server port^>-^<high server port^>]
pause

:Done
endlocal
