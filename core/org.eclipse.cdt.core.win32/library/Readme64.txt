#*******************************************************************************
# Copyright (c) 2011 Marc-Andre Laperle
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     Marc-Andre Laperle - initial API and implementation
#*******************************************************************************

How to build the Win32 x86_64 core fragment:

1. Prerequisites
- Install Windows SDK 7.1. If the SDK is not installed in the default location or if you want to try a different SDK, modify setenv64.bat accordingly.
- Make sure your JAVA_HOME environment variable is set and points to a 64 bit JDK (C:\Program Files\Java\jdkX.X.X not C:\Program Files (x86)\Java\jdkX.X.X )

2. Build and install
- In a command prompt, execute setenv64.bat. 
The command prompt should turn to a different color and print a message about targeting x64.
- Execute build64.bat 
This will build the dlls and executables, copy them to org.eclipse.cdt.core.win32.x86_64\os\win32\x86_64 then clean the build directories.
Optionally, you can use nmake /f Makefile_x86_64.mk TARGET directly. Refer to Makefile_x86_64.mk for valid targets.
