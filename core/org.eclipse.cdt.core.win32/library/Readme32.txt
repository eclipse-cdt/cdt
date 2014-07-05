#*******************************************************************************
# Copyright (c) 2014 Ericsson and others
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     Marc-Andre Laperle (Ericsson) - initial API and implementation
#*******************************************************************************

How to build the Win32 x86 core fragment:

1. Prerequisites
- Install Windows SDK 7.1. If the SDK is not installed in the default location or if you want to try a different SDK, modify setenv32.bat accordingly.
- Make sure your JAVA_HOME environment variable is set and points to a 32 bit JDK (on 64 bit Windows, it would be C:\Program Files (x86)\Java\jdkX.X.X not C:\Program Files\Java\jdkX.X.X)

2. Build and install
- In a command prompt, execute setenv32.bat. 
The command prompt should turn to a different color and print a message about targeting x86.
- Execute build32.bat 
This will build the dlls and executables, copy them to org.eclipse.cdt.core.win32.x86\os\win32\x86 then clean the build directories.
Optionally, you can use nmake /f Makefile_x86.mk TARGET directly. Refer to Makefile_x86.mk for valid targets.
