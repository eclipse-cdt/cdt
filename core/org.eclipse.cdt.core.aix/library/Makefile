# makefile for libspawner.so

#set JDK_INCLUDES
JDK_INCLUDES=/opt/ibm-jdk1.4.2/include
RM=rm
CC=xlc
CFLAGS = -I. -I$(JDK_INCLUDES)
#CFLAGS +=-fpic  -D_REENTRANT

LIB_NAME_SPAWNER = libspawner.so
LIB_NAME_FULL_SPAWNER = ../os/aix/ppc/libspawner.so
OBJS_SPAWNER=spawner.o io.o exec_unix.o exec_pty.o openpty.o pfind.o

LIB_NAME_PTY = libpty.so
LIB_NAME_FULL_PTY = ../os/aix/ppc/libpty.so
OBJS_PTY=openpty.o pty.o ptyio.o

all: $(LIB_NAME_FULL_SPAWNER) $(LIB_NAME_FULL_PTY)

$(LIB_NAME_FULL_SPAWNER): $(OBJS_SPAWNER)
	mkdir -p ../os/aix/ppc
	$(CC) -G -o $(LIB_NAME_FULL_SPAWNER) $(OBJS_SPAWNER) -lc

$(LIB_NAME_FULL_PTY): $(OBJS_PTY)
	$(CC) -G -o $(LIB_NAME_FULL_PTY) $(OBJS_PTY)

clean :
	$(RM) $(OBJS_SPAWNER) $(LIB_NAME_FULL_SPAWNER)
	$(RM) $(OBJS_PTY) $(LIB_NAME_FULL_PTY)
