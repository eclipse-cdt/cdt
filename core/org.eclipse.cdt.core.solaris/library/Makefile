# makefile for libspawner.so

#set JDK_INCLUDES
JDK_INCLUDES=/usr/java/include
JDK_OS_INCLUDES=/usr/java/include/solaris

CC=gcc
LIB_NAME = libspawner.so
LIB_NAME_FULL = ../os/solaris/sparc/libspawner.so
OBJS=spawner.o io.o exec_unix.o pfind.o
CPPFLAGS = -I. -I$(JDK_INCLUDES) -I$(JDK_OS_INCLUDES)
CFLAGS +=-fpic  -D_REENTRANT

spawner : $(OBJS)
	$(CC) -g -shared -Wl,-soname,$(LIB_NAME) -o $(LIB_NAME_FULL) $(OBJS) -lc

clean :
	rm $(OBJS)
