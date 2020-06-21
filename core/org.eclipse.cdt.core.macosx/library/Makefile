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
#     Alex Blewitt - MacOSX with a 64-bit vm 
# Martin Oberhuber (Wind River) - Bug 476709 - Fix change_window_size()
#     Torbjörn Svensson - Bug 521515 - Adopted jenkins build
#*******************************************************************************/

ifeq ($(JAVA_HOME),)
$(error Please define JAVA_HOME)
endif

CC := x86_64-apple-darwin17-clang
CFLAGS += -I. -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/darwin -fPIC -D_REENTRANT
LDFLAGS += -dynamiclib -lc -framework JavaVM

BUILD_DIR_X86 := build/x86
BUILD_DIR_X86_64 := build/x86_64

INSTALL_DIR_X86 := ../os/macosx/x86
INSTALL_DIR_X86_64 := ../os/macosx/x86_64

LIB_NAME_SPAWNER := libspawner.jnilib
OBJS_SPAWNER := spawner.o io.o exec_unix.o exec_pty.o openpty.o pfind.o
OBJS_SPAWNER_X86 := $(addprefix $(BUILD_DIR_X86)/,$(OBJS_SPAWNER))
OBJS_SPAWNER_X86_64 := $(addprefix $(BUILD_DIR_X86_64)/,$(OBJS_SPAWNER))


LIB_NAME_PTY := libpty.jnilib
OBJS_PTY := openpty.o pty.o ptyio.o
OBJS_PTY_X86 := $(addprefix $(BUILD_DIR_X86)/,$(OBJS_PTY))
OBJS_PTY_X86_64 := $(addprefix $(BUILD_DIR_X86_64)/,$(OBJS_PTY))


all: x86 x86_64

x86: $(addprefix $(INSTALL_DIR_X86)/,$(LIB_NAME_SPAWNER) $(LIB_NAME_PTY))

x86_64: $(addprefix $(INSTALL_DIR_X86_64)/,$(LIB_NAME_SPAWNER) $(LIB_NAME_PTY))

rebuild: clean all

$(INSTALL_DIR_X86) $(INSTALL_DIR_X86_64) $(BUILD_DIR_X86) $(BUILD_DIR_X86_64):
	@mkdir -p $@

$(BUILD_DIR_X86)/%.o: %.c $(BUILD_DIR_X86)
	$(CC) -arch i386 $(CFLAGS) -c -o $@ $<

$(BUILD_DIR_X86_64)/%.o: %.c $(BUILD_DIR_X86_64)
	$(CC) -arch x86_64 $(CFLAGS) -c -o $@ $<

$(INSTALL_DIR_X86)/$(LIB_NAME_SPAWNER) : $(INSTALL_DIR_X86) $(OBJS_SPAWNER_X86)
	$(CC) -arch i386 -o $@ $(OBJS_SPAWNER_X86) $(LDFLAGS)

$(INSTALL_DIR_X86_64)/$(LIB_NAME_SPAWNER) : $(INSTALL_DIR_X86_64) $(OBJS_SPAWNER_X86_64)
	$(CC) -arch x86_64 -o $@ $(OBJS_SPAWNER_X86_64) $(LDFLAGS)

$(INSTALL_DIR_X86)/$(LIB_NAME_PTY) : $(INSTALL_DIR_X86) $(OBJS_PTY_X86)
	$(CC) -arch i386 -o $@ $(OBJS_PTY_X86) $(LDFLAGS)

$(INSTALL_DIR_X86_64)/$(LIB_NAME_PTY) : $(INSTALL_DIR_X86_64) $(OBJS_PTY_X86_64)
	$(CC) -arch x86_64 -o $@ $(OBJS_PTY_X86_64) $(LDFLAGS)

clean :
	$(RM) -r $(BUILD_DIR_X86) $(BUILD_DIR_X86_64)
