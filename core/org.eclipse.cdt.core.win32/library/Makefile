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
$(error JAVA_HOME not set in environment)
endif

CC := x86_64-w64-mingw32-gcc
CXX := x86_64-w64-mingw32-gcc
DEBUG_FLAGS =  -D_UNICODE -DDEBUG_MONITOR -DREAD_REPORT
CFLAGS += -I. -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/win32 -DUNICODE
CXXFLAGS=$(CFLAGS)
LIB_LDFLAGS += -Wl,--kill-at --shared

BUILD_DIR_X86_64 := build/x86_64

INSTALL_DIR_X86_64 := ../../org.eclipse.cdt.core.win32.x86_64/os/win32/x86_64

LIB_NAME_SPAWNER := spawner.dll
OBJS_SPAWNER := Win32ProcessEx.o iostream.o raise.o spawner.o
OBJS_SPAWNER_X86_64 := $(addprefix $(BUILD_DIR_X86_64)/,$(OBJS_SPAWNER))

LIB_NAME_WINREG := winreg.dll
OBJS_WINREG := winreg.o
OBJS_WINREG_X86_64 := $(addprefix $(BUILD_DIR_X86_64)/,$(OBJS_WINREG))

EXE_NAME_LISTTASKS := listtasks.exe
OBJS_LISTTASKS := listtasks.o
OBJS_LISTTASKS_X86_64 := $(addprefix $(BUILD_DIR_X86_64)/,$(OBJS_LISTTASKS))

EXE_NAME_STARTER := starter.exe
OBJS_STARTER := starter.o
OBJS_STARTER_X86_64 := $(addprefix $(BUILD_DIR_X86_64)/,$(OBJS_STARTER))

all: x86_64

x86_64: $(addprefix $(INSTALL_DIR_X86_64)/,$(LIB_NAME_SPAWNER) $(LIB_NAME_WINREG) $(EXE_NAME_LISTTASKS) $(EXE_NAME_STARTER))

rebuild: clean all

$(INSTALL_DIR_X86_64) $(BUILD_DIR_X86_64):
	@mkdir -p $@

$(BUILD_DIR_X86_64)/%.o: %.c $(BUILD_DIR_X86_64)
	$(CC) $(CFLAGS) -c -o $@ $<

$(INSTALL_DIR_X86_64)/$(LIB_NAME_SPAWNER): $(INSTALL_DIR_X86_64) $(OBJS_SPAWNER_X86_64)
	$(CC) $(LDFLAGS) $(LIB_LDFLAGS) -o $@ $(OBJS_SPAWNER_X86_64)

$(INSTALL_DIR_X86_64)/$(LIB_NAME_WINREG): $(INSTALL_DIR_X86_64) $(OBJS_WINREG_X86_64)
	$(CC) $(LDFLAGS) $(LIB_LDFLAGS) -o $@ $(OBJS_WINREG_X86_64)

$(INSTALL_DIR_X86_64)/$(EXE_NAME_LISTTASKS): $(INSTALL_DIR_X86_64) $(OBJS_LISTTASKS_X86_64)
	$(CC) $(LDFLAGS) -o $@ $(OBJS_LISTTASKS_X86_64)

$(INSTALL_DIR_X86_64)/$(EXE_NAME_STARTER): $(INSTALL_DIR_X86_64) $(OBJS_STARTER_X86_64)
	$(CC) $(LDFLAGS) -o $@ $(OBJS_STARTER_X86_64) -lpsapi

clean :
	$(RM) -r $(BUILD_DIR_X86_64)
