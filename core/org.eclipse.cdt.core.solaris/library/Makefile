# makefile for libspawner.so

#set JDK_INCLUDES
JDK_INCLUDES=/usr/java/include
JDK_OS_INCLUDES=/usr/java/include/solaris

CC=gcc
CPPFLAGS = -I. -I$(JDK_INCLUDES) -I$(JDK_OS_INCLUDES)
CFLAGS +=-fpic  -D_REENTRANT

LIB_NAME_SPAWNER = libspawner.so
LIB_NAME_FULL_SPAWNER = ../os/solaris/sparc/libspawner.so
OBJS_SPAWNER=spawner.o io.o exec_unix.o exec_pty.o openpty.o pfind.o

LIB_NAME_PTY = libpty.so
LIB_NAME_FULL_PTY = ../os/solaris/sparc/libpty.so
OBJS_PTY=openpty.o pty.o ptyio.o

all: $(LIB_NAME_FULL_SPAWNER) $(LIB_NAME_FULL_PTY)

$(LIB_NAME_FULL_SPAWNER): $(OBJS_SPAWNER)
	$(CC) -g -shared -Wl,-soname,$(LIB_NAME_SPAWNER) -o $(LIB_NAME_FULL_SPAWNER) $(OBJS_SPAWNER) -lc

$(LIB_NAME_FULL_PTY): $(OBJS_PTY)
	$(CC) -g -shared -Wl,-soname,$(LIB_NAME_PTY) -o $(LIB_NAME_FULL_PTY) $(OBJS_PTY)

clean :
	$(RM) $(OBJS_SPAWNER) $(LIB_NAME_FULL_SPAWNER)
	$(RM) $(OBJS_PTY) $(LIB_NAME_FULL_PTY)
