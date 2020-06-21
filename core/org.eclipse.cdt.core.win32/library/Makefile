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
LDFLAGS += -Wl,--kill-at --shared

BUILD_DIR_X86_64 := build/x86_64

INSTALL_DIR_X86_64 = ../../org.eclipse.cdt.core.win32.x86_64/os/win32/x86_64

LIB_NAME_SPAWNER = spawner.dll
OBJS_SPAWNER := StdAfx.o Win32ProcessEx.o iostream.o raise.o spawner.o
OBJS_SPAWNER_X86_64 := $(addprefix $(BUILD_DIR_X86_64)/,$(OBJS_SPAWNER))

all: x86_64

x86_64: $(addprefix $(INSTALL_DIR_X86_64)/,$(LIB_NAME_SPAWNER))

rebuild: clean all

$(INSTALL_DIR_X86_64) $(BUILD_DIR_X86_64):
	@mkdir -p $@

$(BUILD_DIR_X86_64)/%.o: %.c $(BUILD_DIR_X86_64)
	$(CC) $(CFLAGS) -c -o $@ $<

$(BUILD_DIR_X86_64)%.o: %.cpp $(BUILD_DIR_X86_64)
	$(CXX) $(CXXFLAGS) -c -o $@ $<

$(INSTALL_DIR_X86_64)/$(LIB_NAME_SPAWNER) : $(INSTALL_DIR_X86_64) $(OBJS_SPAWNER_X86_64)
	$(CC) $(LDFLAGS) -o $@ $(OBJS_SPAWNER_X86_64)

clean :
	$(RM) -r $(BUILD_DIR_X86_64)
