# makefile for libspawner.so

ifeq ($(JAVA_HOME),)
$(error JAVA_HOME not set in environment)
endif

# Defaults which can be overridden.
OS = win32
ARCH = x86

JDK_INCLUDES= "$(JAVA_HOME)/include"
JDK_OS_INCLUDES= "$(JAVA_HOME)/include/$(OS)"

CC=g++
DEBUG_FLAGS =  -D_UNICODE -DDEBUG_MONITOR -DREAD_REPORT
CFLAGS = -DUNICODE -I$(JDK_INCLUDES) -I$(JDK_OS_INCLUDES)
CXX=g++
CXXFLAGS=$(CFLAGS)

INSTALL_DIR = ../../org.eclipse.cdt.core.$(OS).$(ARCH)/os/$(OS)/$(ARCH)

LIB_NAME_SPAWNER = spawner.dll
LIB_NAME_FULL_SPAWNER = $(INSTALL_DIR)/spawner.dll
OBJS_SPAWNER=StdAfx.o Win32ProcessEx.o iostream.o raise.o spawner.o

all: $(LIB_NAME_FULL_SPAWNER)

rebuild: clean all

$(LIB_NAME_FULL_SPAWNER) : $(OBJS_SPAWNER)
	$(CC) -Wl,--kill-at -shared -o $(LIB_NAME_FULL_SPAWNER) $(OBJS_SPAWNER) -lpsapi

clean :
	$(RM) $(OBJS_SPAWNER)
