#*******************************************************************************
# Copyright (c) 2002, 2015 QNX Software Systems and others.
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
# Martin Oberhuber (Wind River) - Bug 476709 - Fix change_window_size()
#*******************************************************************************/

# makefile for libspawner.so
# See http://developer.apple.com/documentation/Java/Conceptual/Java141Development/Core_APIs/chapter_6_section_4.html

JAVA_HOME = $(shell echo /Library/Java/JavaVirtualMachines/jdk1.[78].0_*.jdk/Contents/Home)

# Defaults which can be overridden.
OS = macosx
ARCH_X86 = x86
ARCH_X86_64 = x86_64

CC=gcc
LD=libtool
CPPFLAGS = -I. -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/darwin
CFLAGS +=-fPIC -D_REENTRANT

ARCH_FLAG_X86 = -arch i386
ARCH_FLAG_X86_64 = -arch x86_64

INSTALL_DIR_X86 = ../os/$(OS)/$(ARCH_X86)
INSTALL_DIR_X86_64 = ../os/$(OS)/$(ARCH_X86_64)

LIB_NAME_FULL_SPAWNER_X86 = $(INSTALL_DIR_X86)/libspawner.jnilib
LIB_NAME_FULL_SPAWNER_X86_64 = $(INSTALL_DIR_X86_64)/libspawner.jnilib
OBJS_SPAWNER_X86 = spawner_$(ARCH_X86).o \
                   io_$(ARCH_X86).o \
                   exec_unix_$(ARCH_X86).o \
                   exec_pty_$(ARCH_X86).o \
                   openpty_$(ARCH_X86).o \
                   pfind_$(ARCH_X86).o
OBJS_SPAWNER_X86_64 = spawner_$(ARCH_X86_64).o \
                   io_$(ARCH_X86_64).o \
                   exec_unix_$(ARCH_X86_64).o \
                   exec_pty_$(ARCH_X86_64).o \
                   openpty_$(ARCH_X86_64).o \
                   pfind_$(ARCH_X86_64).o

LIB_NAME_FULL_PTY_X86 = $(INSTALL_DIR_X86)/libpty.jnilib
LIB_NAME_FULL_PTY_X86_64 = $(INSTALL_DIR_X86_64)/libpty.jnilib
OBJS_PTY_X86 = openpty_$(ARCH_X86).o pty_$(ARCH_X86).o ptyio_$(ARCH_X86).o
OBJS_PTY_X86_64 = openpty_$(ARCH_X86_64).o pty_$(ARCH_X86_64).o ptyio_$(ARCH_X86_64).o

OBJS_X86 = $(OBJS_SPAWNER_X86) $(OBJS_PTY_X86) $(OBJS_SERIAL_X86)
OBJS_X86_64 = $(OBJS_SPAWNER_X86_64) $(OBJS_PTY_X86_64) $(OBJS_SERIAL_X86_64)

all: x86 x86_64

x86: $(LIB_NAME_FULL_SPAWNER_X86) $(LIB_NAME_FULL_PTY_X86)

x86_64: $(LIB_NAME_FULL_SPAWNER_X86_64) $(LIB_NAME_FULL_PTY_X86_64)

rebuild: clean all

$(LIB_NAME_FULL_SPAWNER_X86) : $(OBJS_SPAWNER_X86)
	mkdir -p $(INSTALL_DIR_X86)
	$(CC) -dynamiclib $(ARCH_FLAG_X86) -o $(LIB_NAME_FULL_SPAWNER_X86) $(OBJS_SPAWNER_X86) -lc -framework JavaVM

$(LIB_NAME_FULL_SPAWNER_X86_64) : $(OBJS_SPAWNER_X86_64)
	mkdir -p $(INSTALL_DIR_X86_64)
	$(CC) -dynamiclib $(ARCH_FLAG_X86_64) -o $(LIB_NAME_FULL_SPAWNER_X86_64) $(OBJS_SPAWNER_X86_64) -lc -framework JavaVM

$(LIB_NAME_FULL_PTY_X86): $(OBJS_PTY_X86)
	mkdir -p $(INSTALL_DIR_X86)
	$(CC) -dynamiclib  $(ARCH_FLAG_X86) -o $(LIB_NAME_FULL_PTY_X86) $(OBJS_PTY_X86) -lc -framework JavaVM

$(LIB_NAME_FULL_PTY_X86_64): $(OBJS_PTY_X86_64)
	mkdir -p $(INSTALL_DIR_X86_64)
	$(CC) -dynamiclib  $(ARCH_FLAG_X86_64) -o $(LIB_NAME_FULL_PTY_X86_64) $(OBJS_PTY_X86_64) -lc -framework JavaVM

spawner_$(ARCH_X86).o: spawner.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86) $(CPPFLAGS) -c -o $@ spawner.c

io_$(ARCH_X86).o: io.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86) $(CPPFLAGS) -c -o $@ io.c

exec_unix_$(ARCH_X86).o: exec_unix.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86) $(CPPFLAGS) -c -o $@ exec_unix.c

exec_pty_$(ARCH_X86).o: exec_pty.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86) $(CPPFLAGS) -c -o $@ exec_pty.c

openpty_$(ARCH_X86).o: openpty.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86) $(CPPFLAGS) -c -o $@ openpty.c

pfind_$(ARCH_X86).o: pfind.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86) $(CPPFLAGS) -c -o $@ pfind.c

pty_$(ARCH_X86).o: pty.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86) $(CPPFLAGS) -c -o $@ pty.c

ptyio_$(ARCH_X86).o: ptyio.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86) $(CPPFLAGS) -c -o $@ ptyio.c

spawner_$(ARCH_X86_64).o: spawner.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86_64) $(CPPFLAGS) -c -o $@ spawner.c

io_$(ARCH_X86_64).o: io.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86_64) $(CPPFLAGS) -c -o $@ io.c

exec_unix_$(ARCH_X86_64).o: exec_unix.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86_64) $(CPPFLAGS) -c -o $@ exec_unix.c

exec_pty_$(ARCH_X86_64).o: exec_pty.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86_64) $(CPPFLAGS) -c -o $@ exec_pty.c

openpty_$(ARCH_X86_64).o: openpty.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86_64) $(CPPFLAGS) -c -o $@ openpty.c

pfind_$(ARCH_X86_64).o: pfind.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86_64) $(CPPFLAGS) -c -o $@ pfind.c

pty_$(ARCH_X86_64).o: pty.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86_64) $(CPPFLAGS) -c -o $@ pty.c

ptyio_$(ARCH_X86_64).o: ptyio.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86_64) $(CPPFLAGS) -c -o $@ ptyio.c

clean :
	$(RM) $(OBJS_X86) $(LIB_NAME_FULL_SPAWNER_X86) $(LIB_NAME_FULL_PTY_X86)
	$(RM) $(OBJS_X86_64) $(LIB_NAME_FULL_SPAWNER_X86_64) $(LIB_NAME_FULL_PTY_X86_64)
