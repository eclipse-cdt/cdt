#*******************************************************************************
# Copyright (c) 2005, 2006 QNX Software Systems
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     QNX Software Systems - initial API and implementation
#*******************************************************************************
TARGET = ..\os\win32\x86\winreg.dll

OBJS = winreg.obj

CPPFLAGS = /nologo /I C:\Java\jdk1.5.0_06\include /I C:\Java\jdk1.5.0_06\include\win32 /DUNICODE

$(TARGET):	$(OBJS)
	link /nologo /dll /out:$(TARGET) $(OBJS) advapi32.lib user32.lib
