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

UNAME = $(shell uname)

# Defaults which can be overridden.
ifeq ($(UNAME),Darwin)
JAVA_HOME = $(shell echo /Library/Java/JavaVirtualMachines/jdk1.8.0_*.jdk/Contents/Home)
OS = macosx
JNI_OS = darwin
ARCHS = x86_64
ARCH_FLAG_X86 = -arch i386
ARCH_FLAG_X86_64 = -arch x86_64
LDFLAGS = -dynamiclib
LIBEXT = jnilib
else
ifeq ($(UNAME),Linux)
JAVA_HOME = /usr/lib/jvm/java-8-oracle
OS = linux
JNI_OS = linux
ARCHS = x86 x86_64
ARCH_FLAG_X86 = -m32
ARCH_FLAG_X86_64 = -m64
LDFLAGS = -shared
LIBEXT = so
endif
endif

ARCH_X86 = x86
ARCH_X86_64 = x86_64

CC=gcc
LD=libtool
CPPFLAGS = -I. -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/$(JNI_OS)
CFLAGS +=-fPIC -D_REENTRANT

INSTALL_DIR_X86 = ../../os/$(OS)/$(ARCH_X86)
INSTALL_DIR_X86_64 = ../../os/$(OS)/$(ARCH_X86_64)

LIB_NAME_FULL_SERIAL_X86 = $(INSTALL_DIR_X86)/libserial.$(LIBEXT)
LIB_NAME_FULL_SERIAL_X86_64 = $(INSTALL_DIR_X86_64)/libserial.$(LIBEXT)

OBJS_SERIAL_X86 = serial_$(ARCH_X86).o
OBJS_SERIAL_X86_64 = serial_$(ARCH_X86_64).o

OBJS_X86 = $(OBJS_SERIAL_X86)
OBJS_X86_64 = $(OBJS_SERIAL_X86_64)

all: $(ARCHS)

x86: $(LIB_NAME_FULL_SERIAL_X86)

x86_64:  $(LIB_NAME_FULL_SERIAL_X86_64)

rebuild: clean all

$(LIB_NAME_FULL_SERIAL_X86): $(OBJS_SERIAL_X86)
	mkdir -p $(INSTALL_DIR_X86)
	$(CC) $(LDFLAGS)  $(ARCH_FLAG_X86) -o $(LIB_NAME_FULL_SERIAL_X86) $(OBJS_SERIAL_X86)

$(LIB_NAME_FULL_SERIAL_X86_64): $(OBJS_SERIAL_X86_64)
	mkdir -p $(INSTALL_DIR_X86_64)
	$(CC) $(LDFLAGS)  $(ARCH_FLAG_X86_64) -o $(LIB_NAME_FULL_SERIAL_X86_64) $(OBJS_SERIAL_X86_64)

serial_$(ARCH_X86).o: serial.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86) $(CPPFLAGS) -c -o $@ serial.c

serial_$(ARCH_X86_64).o: serial.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86_64) $(CPPFLAGS) -c -o $@ serial.c

clean :
	$(RM) $(OBJS_X86)
	$(RM) $(OBJS_X86_64)
