# makefile for libspawner.so

ifeq ($(JAVA_HOME),)
$(warning JAVA_HOME not set in environment)
endif

# Defaults which can be overridden.
OS = win32
ARCH = x86

JDK_INCLUDES= $(JAVA_HOME)/include
JDK_OS_INCLUDES= $(JAVA_HOME)/include/$(OS)

CC=gcc
CPPFLAGS = -I. -I$(JDK_INCLUDES) -I$(JDK_OS_INCLUDES)
CFLAGS += -D_UNICODE -Dwchar_t=short

INSTALL_DIR = ../os/$(OS)/$(ARCH)

LIB_NAME_SPAWNER = spawner.dll
LIB_NAME_FULL_SPAWNER = $(INSTALL_DIR)/spawner.dll
OBJS_SPAWNER=StdAfx.o Win32ProcessEx.o iostream.o raise.o spawner.o

OBJS = $(OBJS_SPAWNER)

all: $(LIB_NAME_FULL_SPAWNER)

rebuild: clean all

$(LIB_NAME_FULL_SPAWNER) : $(OBJS_SPAWNER)
	mkdir -p $(INSTALL_DIR)
	$(CC) -Wl,--kill-at -g -shared -o $(LIB_NAME_FULL_SPAWNER) $(OBJS_SPAWNER)

clean :
	$(RM) $(OBJS_SPAWNER) $(LIB_NAME_FULL_SPAWNER)
