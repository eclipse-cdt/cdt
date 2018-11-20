#*******************************************************************************
# Copyright (c) 2011 Marc-Andre Laperle
#
# This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#     Marc-Andre Laperle - initial API and implementation
#*******************************************************************************

# Makefile (nmake) for Core fragment on Windows x86_64

OS = win32
ARCH = x86_64

JDK_INCLUDES= "$(JAVA_HOME)\include"
JDK_OS_INCLUDES= "$(JAVA_HOME)\include/$(OS)"

CC=cl
DEBUG_FLAGS =  -DDEBUG_MONITOR -DREAD_REPORT
CFLAGS = /TP /I$(JDK_INCLUDES) /I$(JDK_OS_INCLUDES) /EHs /nologo
CFLAGS_UNICODE = /D "_UNICODE" /D "UNICODE" $(CFLAGS)

INSTALL_DIR = ..\..\org.eclipse.cdt.core.$(OS).$(ARCH)\os\$(OS)\$(ARCH)

DLL_SPAWNER = spawner.dll
OBJS_SPAWNER=StdAfx.obj Win32ProcessEx.obj iostream.obj raise.obj spawner.obj

DLL_WINREG = winreg.dll
OBJS_WINREG=winreg/winreg.obj

EXE_STARTER = starter.exe
OBJS_STARTER=starter/starter.obj

EXE_LISTTASKS = listtasks.exe
OBJS_LISTTASKS=listtasks/listtasks.obj listtasks/StdAfx.obj

.c.obj:
	cl /c $(CFLAGS_UNICODE) $*.c /Fo$@

.cpp.obj:
	cl /c $(CFLAGS_UNICODE) $*.cpp /Fo$@

#TODO: Use unicode for listtasks, see bug 353460
listtasks/listtasks.obj:
	cl /c $(CFLAGS) $*.cpp /Fo$@

spawner: $(OBJS_SPAWNER)
	link /dll /nologo /out:$(DLL_SPAWNER) $(OBJS_SPAWNER) User32.lib

winreg: $(OBJS_WINREG)
	link /dll /nologo /out:$(DLL_WINREG) $(OBJS_WINREG) Advapi32.lib
	
starter: $(OBJS_STARTER)
	link /nologo /out:$(EXE_STARTER) $(OBJS_STARTER) Psapi.Lib Shell32.lib
	
listtasks: $(OBJS_LISTTASKS)
	link /nologo /out:$(EXE_LISTTASKS) $(OBJS_LISTTASKS) Psapi.Lib

all: spawner winreg starter listtasks

clean:
	del *.obj *.lib *.exp *.exe *.dll winreg\*.obj starter\*.obj listtasks\*.obj

rebuild: clean all

install: all
	copy *.dll $(INSTALL_DIR)
	copy *.exe $(INSTALL_DIR)
	
uninstall:
	del $(INSTALL_DIR)\*.dll $(INSTALL_DIR)\*.exe
