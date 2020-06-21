#*******************************************************************************
# Copyright (c) 2002, 2015, 2020 QNX Software Systems and others.
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
#     Torbjörn Svensson - Bug 521515 - Adopted jenkins build
#*******************************************************************************/

ifeq ($(JAVA_HOME),)
$(warning JAVA_HOME not set in environment)
endif

CC := gcc
CFLAGS += -I. -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -fpic -D_REENTRANT -D_GNU_SOURCE
LDFLAGS += -g -shared -lc

BUILD_DIR_X86_64 := build/x86_64

INSTALL_DIR_X86_64 = ../../org.eclipse.cdt.core.linux.x86_64/os/linux/x86_64

LIB_NAME_SPAWNER := libspawner.so
OBJS_SPAWNER := spawner.o io.o exec_unix.o exec_pty.o openpty.o pfind.o
OBJS_SPAWNER_X86_64 := $(addprefix $(BUILD_DIR_X86_64)/,$(OBJS_SPAWNER))


LIB_NAME_PTY := libpty.so
OBJS_PTY := openpty.o pty.o ptyio.o
OBJS_PTY_X86_64 := $(addprefix $(BUILD_DIR_X86_64)/,$(OBJS_PTY))


all: x86_64

x86_64: $(addprefix $(INSTALL_DIR_X86_64)/,$(LIB_NAME_SPAWNER) $(LIB_NAME_PTY))

rebuild: clean all

$(INSTALL_DIR_X86_64) $(BUILD_DIR_X86_64):
	@mkdir -p $@

$(BUILD_DIR_X86_64)/%.o: %.c $(BUILD_DIR_X86_64)
	$(CC) $(CFLAGS) -m64 -c -o $@ $<

$(INSTALL_DIR_X86_64)/$(LIB_NAME_SPAWNER) : $(INSTALL_DIR_X86_64) $(OBJS_SPAWNER_X86_64)
	$(CC) -m64 -Wl,-soname,$(LIB_NAME_SPAWNER) $(LDFLAGS) -o $@ $(OBJS_SPAWNER_X86_64)

$(INSTALL_DIR_X86_64)/$(LIB_NAME_PTY) : $(INSTALL_DIR_X86_64) $(OBJS_PTY_X86_64)
	$(CC) -m64 -Wl,-soname,$(LIB_NAME_PTY) $(LDFLAGS) -o $@ $(OBJS_PTY_X86_64)

clean :
	$(RM) -r $(BUILD_DIR_X86_64)
