# makefile for libspawner.so

ifeq ($(JAVA_HOME),)
$(warning JAVA_HOME not set in environment)
endif

# Defaults which can be overridden.
OS = win32
ARCH = x86

JDK_INCLUDES= "$(JAVA_HOME)/include"
JDK_OS_INCLUDES= "$(JAVA_HOME)/include/$(OS)"

CC=gcc
CFLAGS = -DUNICODE -I$(JDK_INCLUDES) -I$(JDK_OS_INCLUDES)
CXX=g++
CXXFLAGS=$(CFLAGS)

INSTALL_DIR = ../os/$(OS)/$(ARCH)

LIB_NAME_SPAWNER = spawner.dll
LIB_NAME_FULL_SPAWNER = $(INSTALL_DIR)/spawner.dll
OBJS_SPAWNER=StdAfx.o Win32ProcessEx.o iostream.o raise.o spawner.o

WINREG = $(INSTALL_DIR)/winreg.dll
OBJS_WINREG = winreg.o

all: $(LIB_NAME_FULL_SPAWNER) $(WINREG)

rebuild: clean all

$(LIB_NAME_FULL_SPAWNER) : $(OBJS_SPAWNER)
	$(CC) -Wl,--kill-at -shared -o $(LIB_NAME_FULL_SPAWNER) $(OBJS_SPAWNER)

clean :
	$(RM) $(OBJS_SPAWNER) $(LIB_NAME_FULL_SPAWNER) $(OBJS_WINREG) $(WINREG)

$(WINREG) : $(OBJS_WINREG)
	$(CXX) -Wl,--kill-at -shared -o $(WINREG) $(OBJS_WINREG)
