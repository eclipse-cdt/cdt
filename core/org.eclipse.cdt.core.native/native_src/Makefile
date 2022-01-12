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
SHELL=/bin/bash -o pipefail
ifeq ($(JAVA_HOME),)
$(error JAVA_HOME not set in environment)
endif

REPRODUCIBLE_BUILD_WRAPPER := $(shell git rev-parse --show-toplevel)/releng/scripts/reproducible_build_wrapper.py

OS_DIR_WIN32_X86_64 := ../../org.eclipse.cdt.core.win32.x86_64/os/win32/x86_64
OS_DIR_LINUX_X86_64 := ../../org.eclipse.cdt.core.linux.x86_64/os/linux/x86_64
OS_DIR_LINUX_AARCH64 := ../../org.eclipse.cdt.core.linux.aarch64/os/linux/aarch64
OS_DIR_LINUX_PPC64LE := ../../org.eclipse.cdt.core.linux.ppc64le/os/linux/ppc64le
OS_DIR_MACOS_X86_64 := ../../org.eclipse.cdt.core.macosx/os/macosx/x86_64
OS_DIR_MACOS_AARCH64 := ../../org.eclipse.cdt.core.macosx/os/macosx/aarch64

COMMON_CFLAGS :=  -Wall -pedantic -Werror

UNAME = $(shell uname)
ifeq ($(UNAME),Linux)
LIBS = \
	$(OS_DIR_WIN32_X86_64)/starter.exe \
	$(OS_DIR_WIN32_X86_64)/spawner.dll \
	$(OS_DIR_WIN32_X86_64)/pty.dll \
	$(OS_DIR_LINUX_X86_64)/libspawner.so \
	$(OS_DIR_LINUX_X86_64)/libpty.so \
	$(OS_DIR_LINUX_AARCH64)/libspawner.so \
	$(OS_DIR_LINUX_AARCH64)/libpty.so \
	$(OS_DIR_LINUX_PPC64LE)/libspawner.so \
	$(OS_DIR_LINUX_PPC64LE)/libpty.so \
	$(OS_DIR_MACOS_X86_64)/libspawner.jnilib \
	$(OS_DIR_MACOS_X86_64)/libpty.jnilib \
	$(OS_DIR_MACOS_AARCH64)/libspawner.jnilib \
	$(OS_DIR_MACOS_AARCH64)/libpty.jnilib
else
ifeq ($(UNAME),Darwin)
LIBS = \
	$(OS_DIR_MACOS_X86_64)/libspawner.jnilib \
	$(OS_DIR_MACOS_X86_64)/libpty.jnilib \
	$(OS_DIR_MACOS_AARCH64)/libspawner.jnilib \
	$(OS_DIR_MACOS_AARCH64)/libpty.jnilib
else
LIBS = \
	$(OS_DIR_WIN32_X86_64)/starter.exe \
	$(OS_DIR_WIN32_X86_64)/spawner.dll \
	$(OS_DIR_WIN32_X86_64)/pty.dll
endif
endif

all:	$(LIBS)

clean :
	$(RM) $(LIBS)

rebuild: clean all

MAC_TO_SIGN=$(OS_DIR_MACOS_X86_64)/libspawner.jnilib \
	$(OS_DIR_MACOS_X86_64)/libpty.jnilib \
	$(OS_DIR_MACOS_AARCH64)/libspawner.jnilib \
	$(OS_DIR_MACOS_AARCH64)/libpty.jnilib
WIN_TO_SIGN=$(OS_DIR_WIN32_X86_64)/starter.exe \
	$(OS_DIR_WIN32_X86_64)/spawner.dll \
	$(OS_DIR_WIN32_X86_64)/pty.dll
### This block of code also exists in native/org.eclipse.cdt.native.serial/native_src/Makefile
TMPDIR := $(shell mktemp -d -t production-XXXXXXXXXX)
.PHONY: production
production: $(MAC_TO_SIGN) $(WIN_TO_SIGN)
	set -x ; $(foreach tosign,$(MAC_TO_SIGN),\
		temp=$(TMPDIR)/$(shell basename $(tosign)) && \
		mv $(tosign) $$temp && \
		curl -f --silent --show-error -o $(tosign) -F file=@$$temp https://cbi.eclipse.org/macos/codesign/sign && \
		rm $$temp && \
		) true
	set -x ; $(foreach tosign,$(WIN_TO_SIGN),\
		temp=$(TMPDIR)/$(shell basename $(tosign)) && \
		mv $(tosign) $$temp && \
		curl -f --silent --show-error -o $(tosign) -F file=@$$temp https://cbi.eclipse.org/authenticode/sign && \
		rm $$temp && \
		) true
	rmdir $(TMPDIR)


# Windows x86_64
# Windows DLLs have a build timestamp in them. This makes it impossible to have reproducible builds.
# However, x86_64-w64-mingw32-ld on Debian/Ubuntu has a patch that overrides the current date
# using the SOURCE_DATE_EPOCH environment variable. Call REPRODUCIBLE_BUILD_WRAPPER to make sure the
# same binary is produced for the same source each time.
$(OS_DIR_WIN32_X86_64)/starter.exe: win/starter.c win/util.c
	mkdir -p $(dir $@) && \
	$(REPRODUCIBLE_BUILD_WRAPPER) \
	x86_64-w64-mingw32-gcc $(COMMON_CFLAGS) -o $@ -Iinclude -I"$(JAVA_HOME)/include" -I"$(JAVA_HOME)/include/win32" \
		-DUNICODE \
		$^ \
		-lpsapi

$(OS_DIR_WIN32_X86_64)/spawner.dll: win/iostream.c win/raise.c win/spawner.c win/Win32ProcessEx.c win/util.c
	mkdir -p $(dir $@) && \
	$(REPRODUCIBLE_BUILD_WRAPPER) \
	x86_64-w64-mingw32-gcc $(COMMON_CFLAGS) -o $@ -Iinclude -I"$(JAVA_HOME)/include" -I"$(JAVA_HOME)/include/win32" \
		-DUNICODE \
		$^ \
		-Wl,--kill-at --shared

$(OS_DIR_WIN32_X86_64)/pty.dll: win/pty.cpp win/pty_dllmain.cpp  win/util.c
	mkdir -p $(dir $@) && \
	$(REPRODUCIBLE_BUILD_WRAPPER) \
	x86_64-w64-mingw32-g++ $(COMMON_CFLAGS) -o $@ -Iinclude -Iwin/include -I"$(JAVA_HOME)/include" -I"$(JAVA_HOME)/include/win32" \
		-DUNICODE \
		$^ \
		-Wl,--kill-at --shared -L$(OS_DIR_WIN32_X86_64) -lwinpty -static-libstdc++ -static-libgcc

# Linux x86_64
$(OS_DIR_LINUX_X86_64)/libspawner.so: unix/spawner.c unix/io.c unix/exec_unix.c unix/exec_pty.c unix/openpty.c unix/pfind.c
	mkdir -p $(dir $@) && \
	x86_64-linux-gnu-gcc $(COMMON_CFLAGS) -m64 -o $@ -Wl,-soname,$(notdir $@) -Iinclude -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -fpic \
		-D_REENTRANT -D_GNU_SOURCE \
		$^ \
		-shared -lc

$(OS_DIR_LINUX_X86_64)/libpty.so: unix/openpty.c unix/pty.c unix/ptyio.c
	mkdir -p $(dir $@) && \
	x86_64-linux-gnu-gcc $(COMMON_CFLAGS) -m64 -o $@ -Wl,-soname,$(notdir $@) -Iinclude -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -fpic \
		-D_REENTRANT -D_GNU_SOURCE \
		$^ \
		-shared -lc

# Linux aarch64
$(OS_DIR_LINUX_AARCH64)/libspawner.so: unix/spawner.c unix/io.c unix/exec_unix.c unix/exec_pty.c unix/openpty.c unix/pfind.c
	mkdir -p $(dir $@) && \
	aarch64-linux-gnu-gcc $(COMMON_CFLAGS) -o $@ -Wl,-soname,$(notdir $@) -Iinclude -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -fpic \
		-D_REENTRANT -D_GNU_SOURCE \
		$^ \
		-shared -lc

$(OS_DIR_LINUX_AARCH64)/libpty.so: unix/openpty.c unix/pty.c unix/ptyio.c
	mkdir -p $(dir $@) && \
	aarch64-linux-gnu-gcc $(COMMON_CFLAGS) -o $@ -Wl,-soname,$(notdir $@) -Iinclude -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -fpic \
		-D_REENTRANT -D_GNU_SOURCE \
		$^ \
		-shared -lc

# Linux ppc64le
$(OS_DIR_LINUX_PPC64LE)/libspawner.so: unix/spawner.c unix/io.c unix/exec_unix.c unix/exec_pty.c unix/openpty.c unix/pfind.c
	mkdir -p $(dir $@) && \
	powerpc64le-linux-gnu-gcc $(COMMON_CFLAGS) -m64 -o $@ -Wl,-soname,$(notdir $@) -Iinclude -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -fpic \
		-D_REENTRANT -D_GNU_SOURCE \
		$^ \
		-shared -lc

$(OS_DIR_LINUX_PPC64LE)/libpty.so: unix/openpty.c unix/pty.c unix/ptyio.c
	mkdir -p $(dir $@) && \
	powerpc64le-linux-gnu-gcc -m64 -o $@ -Wl,-soname,$(notdir $@) -Iinclude -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -fpic \
		-D_REENTRANT -D_GNU_SOURCE \
		$^ \
		-shared -lc

# macos x86_64
$(OS_DIR_MACOS_X86_64)/libspawner.jnilib: unix/spawner.c unix/io.c unix/exec_unix.c unix/exec_pty.c unix/openpty.c unix/pfind.c
	mkdir -p $(dir $@) && \
	x86_64-apple-darwin21.1-clang $(COMMON_CFLAGS) -o $@ -arch x86_64 -Iinclude -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/darwin -fPIC \
		-D_REENTRANT \
		$^ \
		-dynamiclib -lc

$(OS_DIR_MACOS_X86_64)/libpty.jnilib: unix/openpty.c unix/pty.c unix/ptyio.c
	mkdir -p $(dir $@) && \
	x86_64-apple-darwin21.1-clang $(COMMON_CFLAGS) -o $@ -arch x86_64 -Iinclude -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/darwin -fPIC \
		-D_REENTRANT \
		$^ \
		-dynamiclib -lc

# macos aarch64
$(OS_DIR_MACOS_AARCH64)/libspawner.jnilib: unix/spawner.c unix/io.c unix/exec_unix.c unix/exec_pty.c unix/openpty.c unix/pfind.c
	mkdir -p $(dir $@) && \
	arm64-apple-darwin21.1-clang $(COMMON_CFLAGS) -o $@ -arch arm64 -Iinclude -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/darwin -fPIC \
		-D_REENTRANT \
		$^ \
		-dynamiclib -lc

$(OS_DIR_MACOS_AARCH64)/libpty.jnilib: unix/openpty.c unix/pty.c unix/ptyio.c
	mkdir -p $(dir $@) && \
	arm64-apple-darwin21.1-clang $(COMMON_CFLAGS) -o $@ -arch arm64 -Iinclude -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/darwin -fPIC \
		-D_REENTRANT \
		$^ \
		-dynamiclib -lc
