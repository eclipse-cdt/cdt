# makefile for libspawner.so

ifeq ($(JAVA_HOME),)
$(warning JAVA_HOME not set in environment)
endif

# Defaults which can be overridden.
OS = linux
ARCH = x86

JDK_INCLUDES= $(JAVA_HOME)/include
JDK_OS_INCLUDES= $(JAVA_HOME)/include/$(OS)

CC=gcc
CPPFLAGS = -I. -I$(JDK_INCLUDES) -I$(JDK_OS_INCLUDES)
CFLAGS +=-fpic -D_REENTRANT -D_GNU_SOURCE

INSTALL_DIR = ../../org.eclipse.cdt.core.linux.$(ARCH)/os/$(OS)/$(ARCH)

LIB_NAME_SPAWNER = libspawner.so
LIB_NAME_FULL_SPAWNER = $(INSTALL_DIR)/libspawner.so
OBJS_SPAWNER=spawner.o io.o exec_unix.o exec_pty.o pfind.o openpty.o

LIB_NAME_PTY = libpty.so
LIB_NAME_FULL_PTY = $(INSTALL_DIR)/libpty.so
OBJS_PTY= openpty.o pty.o ptyio.o

OBJS = $(OBJS_SPAWNER) $(OBJS_PTY)

all: $(LIB_NAME_FULL_SPAWNER) $(LIB_NAME_FULL_PTY)

rebuild: clean all

$(LIB_NAME_FULL_SPAWNER) : $(OBJS_SPAWNER)
	mkdir -p $(INSTALL_DIR)
	$(CC) -g -shared -Wl,-soname,$(LIB_NAME_SPAWNER) -o $(LIB_NAME_FULL_SPAWNER) $(OBJS_SPAWNER) -lc

$(LIB_NAME_FULL_PTY): $(OBJS_PTY)
	mkdir -p $(INSTALL_DIR)
	$(CC) -g -shared -Wl,-soname,$(LIB_NAME_PTY) -o $(LIB_NAME_FULL_PTY) $(OBJS_PTY)

clean :
	$(RM) $(OBJS_SPAWNER) $(LIB_NAME_FULL_SPAWNER)
	$(RM) $(OBJS_PTY) $(LIB_NAME_FULL_PTY)
