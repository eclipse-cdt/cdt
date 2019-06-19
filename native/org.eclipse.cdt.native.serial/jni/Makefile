#*******************************************************************************
# Copyright (c) 2002, 2009 QNX Software Systems and others.
#
# This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#     QNX Software Systems - initial API and implementation
#     Alex Blewitt - MacOSX with a 64-bit vm 
#*******************************************************************************/

ifeq ($(JAVA_HOME),)
$(error Please define JAVA_HOME)
endif

OS_DIR = ../os

CFLAGS += -fPIC -D_REENTRANT

UNAME = $(shell uname)
ifeq ($(UNAME),Linux)
LIBS = \
	$(OS_DIR)/win32/x86_64/serial.dll \
	$(OS_DIR)/linux/x86_64/libserial.so
else
ifeq ($(UNAME),Darwin)
LIBS = \
	$(OS_DIR)/macosx/x86_64/libserial.jnilib
else
LIBS = \
	$(OS_DIR)/win32/x86_64/serial.dll
endif
endif

all:	$(LIBS)

clean :
	$(RM) $(LIBS)

rebuild: clean all

$(OS_DIR)/win32/x86_64/serial.dll:	serial.c
	mkdir -p $(dir $@)
	x86_64-w64-mingw32-gcc -I"$(JAVA_HOME)/include" -I"$(JAVA_HOME)/include/win32" -shared -o $@ serial.c

$(OS_DIR)/linux/x86_64/libserial.so:	serial.c
	mkdir -p $(dir $@)
	gcc -m64 $(CFLAGS) -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux $(LDFLAGS) -shared -o $@ serial.c

$(OS_DIR)/linux/ppc64le/libserial.so:	serial.c
	mkdir -p $(dir $@)
	gcc -m64 -mcpu=power8 $(CFLAGS) -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux $(LDFLAGS) -shared -o $@ serial.c

$(OS_DIR)/macosx/x86_64/libserial.jnilib:	serial.c
	mkdir -p $(dir $@)
	clang $(CFLAGS) -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/darwin $(LDFLAGS) -dynamiclib -o $@ serial.c
