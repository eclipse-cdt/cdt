# makefile for libspawner.so
# See http://developer.apple.com/documentation/Java/Conceptual/Java141Development/Core_APIs/chapter_6_section_4.html

JAVA_HOME = /Library/Java/Home
ifeq ($(JAVA_HOME),)
$(warning JAVA_HOME not set in environment)
endif

# Defaults which can be overridden.
OS = macosx
ARCH = ppc

JDK_INCLUDES= $(JAVA_HOME)/include

CC=gcc
LD=libtool
CPPFLAGS = -I. -I$(JDK_INCLUDES) #-I$(JDK_OS_INCLUDES)
CFLAGS +=-fPIC -D_REENTRANT

INSTALL_DIR = ../os/$(OS)/$(ARCH)

LIB_NAME_SPAWNER = libspawner.dylib
LIB_NAME_FULL_SPAWNER = $(INSTALL_DIR)/libspawner.jnilib
OBJS_SPAWNER=spawner.o io.o exec_unix.o exec_pty.o openpty.o pfind.o

LIB_NAME_PTY = libpty.so
LIB_NAME_FULL_PTY = $(INSTALL_DIR)/libpty.jnilib
OBJS_PTY= openpty.o pty.o ptyio.o

OBJS = $(OBJS_SPAWNER) $(OBJS_PTY)

all: $(LIB_NAME_FULL_SPAWNER) $(LIB_NAME_FULL_PTY)

rebuild: clean all

$(LIB_NAME_FULL_SPAWNER) : $(OBJS_SPAWNER)
	mkdir -p $(INSTALL_DIR)
	$(CC) -dynamiclib -o $(LIB_NAME_FULL_SPAWNER) $(OBJS_SPAWNER) -lc -framework JavaVM

$(LIB_NAME_FULL_PTY): $(OBJS_PTY)
	mkdir -p $(INSTALL_DIR)
	$(CC) -dynamiclib -o $(LIB_NAME_FULL_PTY) $(OBJS_PTY) -lc -framework JavaVM

clean :
	$(RM) $(OBJS_SPAWNER) $(LIB_NAME_FULL_SPAWNER)
	$(RM) $(OBJS_PTY) $(LIB_NAME_FULL_PTY)
