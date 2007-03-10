# makefile for libspawner.so
# See http://developer.apple.com/documentation/Java/Conceptual/Java141Development/Core_APIs/chapter_6_section_4.html

JAVA_HOME = /Library/Java/Home
ifeq ($(JAVA_HOME),)
$(warning JAVA_HOME not set in environment)
endif

# Defaults which can be overridden.
OS = macosx
ARCH_PPC = ppc
ARCH_X86 = x86

JDK_INCLUDES= $(JAVA_HOME)/include

CC=gcc
LD=libtool
CPPFLAGS = -I. -I$(JDK_INCLUDES) #-I$(JDK_OS_INCLUDES)
CFLAGS +=-fPIC -D_REENTRANT

ARCH_FLAG_PPC = -arch ppc
ARCH_FLAG_X86 = -arch i386

INSTALL_DIR_PPC = ../os/$(OS)/$(ARCH_PPC)
INSTALL_DIR_X86 = ../os/$(OS)/$(ARCH_X86)

LIB_NAME_SPAWNER = libspawner.dylib
LIB_NAME_FULL_SPAWNER_PPC = $(INSTALL_DIR_PPC)/libspawner.jnilib
LIB_NAME_FULL_SPAWNER_X86 = $(INSTALL_DIR_X86)/libspawner.jnilib
OBJS_SPAWNER_PPC = spawner_$(ARCH_PPC).o \
                   io_$(ARCH_PPC).o \
                   exec_unix_$(ARCH_PPC).o \
                   exec_pty_$(ARCH_PPC).o \
                   openpty_$(ARCH_PPC).o \
                   pfind_$(ARCH_PPC).o
OBJS_SPAWNER_X86 = spawner_$(ARCH_X86).o \
                   io_$(ARCH_X86).o \
                   exec_unix_$(ARCH_X86).o \
                   exec_pty_$(ARCH_X86).o \
                   openpty_$(ARCH_X86).o \
                   pfind_$(ARCH_X86).o

LIB_NAME_PTY = libpty.so
LIB_NAME_FULL_PTY_PPC = $(INSTALL_DIR_PPC)/libpty.jnilib
LIB_NAME_FULL_PTY_X86 = $(INSTALL_DIR_X86)/libpty.jnilib
OBJS_PTY_PPC = openpty_$(ARCH_PPC).o pty_$(ARCH_PPC).o ptyio_$(ARCH_PPC).o
OBJS_PTY_X86 = openpty_$(ARCH_X86).o pty_$(ARCH_X86).o ptyio_$(ARCH_X86).o

OBJS_PPC = $(OBJS_SPAWNER_PPC) $(OBJS_PTY_PPC)
OBJS_X86 = $(OBJS_SPAWNER_X86) $(OBJS_PTY_X86)

all: ppc x86

ppc: $(LIB_NAME_FULL_SPAWNER_PPC) $(LIB_NAME_FULL_PTY_PPC)

x86: $(LIB_NAME_FULL_SPAWNER_X86) $(LIB_NAME_FULL_PTY_X86)

rebuild: clean all

$(LIB_NAME_FULL_SPAWNER_PPC) : $(OBJS_SPAWNER_PPC)
	mkdir -p $(INSTALL_DIR_PPC)
	$(CC) -dynamiclib $(ARCH_FLAG_PPC) -o $(LIB_NAME_FULL_SPAWNER_PPC) $(OBJS_SPAWNER_PPC) -lc -framework JavaVM

$(LIB_NAME_FULL_SPAWNER_X86) : $(OBJS_SPAWNER_X86)
	mkdir -p $(INSTALL_DIR_X86)
	$(CC) -dynamiclib $(ARCH_FLAG_X86) -o $(LIB_NAME_FULL_SPAWNER_X86) $(OBJS_SPAWNER_X86) -lc -framework JavaVM

$(LIB_NAME_FULL_PTY_PPC): $(OBJS_PTY_PPC)
	mkdir -p $(INSTALL_DIR_PPC)
	$(CC) -dynamiclib  $(ARCH_FLAG_PPC) -o $(LIB_NAME_FULL_PTY_PPC) $(OBJS_PTY_PPC) -lc -framework JavaVM

$(LIB_NAME_FULL_PTY_X86): $(OBJS_PTY_X86)
	mkdir -p $(INSTALL_DIR_X86)
	$(CC) -dynamiclib  $(ARCH_FLAG_X86) -o $(LIB_NAME_FULL_PTY_X86) $(OBJS_PTY_X86) -lc -framework JavaVM

spawner_$(ARCH_PPC).o: spawner.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_PPC) $(CPPFLAGS) -c -o $@ spawner.c

io_$(ARCH_PPC).o: io.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_PPC) $(CPPFLAGS) -c -o $@ io.c

exec_unix_$(ARCH_PPC).o: exec_unix.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_PPC) $(CPPFLAGS) -c -o $@ exec_unix.c

exec_pty_$(ARCH_PPC).o: exec_pty.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_PPC) $(CPPFLAGS) -c -o $@ exec_pty.c

openpty_$(ARCH_PPC).o: openpty.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_PPC) $(CPPFLAGS) -c -o $@ openpty.c

pfind_$(ARCH_PPC).o: pfind.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_PPC) $(CPPFLAGS) -c -o $@ pfind.c

openpty_$(ARCH_PPC).o: openpty.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_PPC) $(CPPFLAGS) -c -o $@ openpty.c

pty_$(ARCH_PPC).o: pty.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_PPC) $(CPPFLAGS) -c -o $@ pty.c

ptyio_$(ARCH_PPC).o: ptyio.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_PPC) $(CPPFLAGS) -c -o $@ ptyio.c

spawner_$(ARCH_X86).o: spawner.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86) $(CPPFLAGS) -c -o $@ spawner.c

io_$(ARCH_X86).o: io.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86) $(CPPFLAGS) -c -o $@ io.c

exec_unix_$(ARCH_X86).o: exec_unix.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86) $(CPPFLAGS) -c -o $@ exec_unix.c

exec_pty_$(ARCH_X86).o: exec_pty.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86) $(CPPFLAGS) -c -o $@ exec_pty.c

openpty_$(ARCH_X86).o: openpty.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86) $(CPPFLAGS) -c -o $@ openpty.c

pfind_$(ARCH_X86).o: pfind.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86) $(CPPFLAGS) -c -o $@ pfind.c

openpty_$(ARCH_X86).o: openpty.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86) $(CPPFLAGS) -c -o $@ openpty.c

pty_$(ARCH_X86).o: pty.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86) $(CPPFLAGS) -c -o $@ pty.c

ptyio_$(ARCH_X86).o: ptyio.c
	$(CC) $(CFLAGS) $(ARCH_FLAG_X86) $(CPPFLAGS) -c -o $@ ptyio.c

clean :
	$(RM) $(OBJS_PPC) $(LIB_NAME_FULL_SPAWNER_PPC) $(LIB_NAME_FULL_SPAWNER_X86)
	$(RM) $(OBJS_X86) $(LIB_NAME_FULL_PTY_PPC) $(LIB_NAME_FULL_PTY_X86)