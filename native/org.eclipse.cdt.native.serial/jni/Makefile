#*******************************************************************************
# Copyright (c) 2002, 2009 QNX Software Systems and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     QNX Software Systems - initial API and implementation
#     Alex Blewitt - MacOSX with a 64-bit vm 
#*******************************************************************************/

ifeq ($(JAVA_HOME),)
$(error Please define JAVA_HOME)
endif

OS_DIR = ../os

UNAME = $(shell uname)
ifeq ($(UNAME),Linux)
LIBS = \
	$(OS_DIR)/win32/x86/serial.dll \
	$(OS_DIR)/win32/x86_64/serial.dll \
	$(OS_DIR)/linux/x86/libserial.so \
	$(OS_DIR)/linux/x86_64/libserial.so
else
ifeq ($(UNAME),Darwin)
LIBS = \
	$(OS_DIR)/macosx/x86_64/libserial.jnilib
else
LIBS = \
	$(OS_DIR)/win32/x86/serial.dll \
	$(OS_DIR)/win32/x86_64/serial.dll
endif
endif

all:	$(LIBS)

clean :
	rm $(LIBS)

rebuild: clean all

$(OS_DIR)/win32/x86/serial.dll:	serial.c
	mkdir -p $(dir $@)
	i686-w64-mingw32-gcc -Wl,--kill-at -I"$(JAVA_HOME)/include" -I"$(JAVA_HOME)/include/win32" -shared -o $@ serial.c

$(OS_DIR)/win32/x86_64/serial.dll:	serial.c
	mkdir -p $(dir $@)
	x86_64-w64-mingw32-gcc -I"$(JAVA_HOME)/include" -I"$(JAVA_HOME)/include/win32" -shared -o $@ serial.c

$(OS_DIR)/linux/x86/libserial.so:	serial.c
	mkdir -p $(dir $@)
	gcc -m32 -fPIC -D_REENTRANT -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -shared -o $@ serial.c

$(OS_DIR)/linux/x86_64/libserial.so:	serial.c
	mkdir -p $(dir $@)
	gcc -m64 -fPIC -D_REENTRANT -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -shared -o $@ serial.c

$(OS_DIR)/macosx/x86_64/libserial.jnilib:	serial.c
	mkdir -p $(dir $@)
	clang -fPIC -D_REENTRANT -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/darwin -dynamiclib -o $@ serial.c
