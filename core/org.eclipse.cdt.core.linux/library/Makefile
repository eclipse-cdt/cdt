# makefile for libspawner.so

LIB_NAME = libspawner.so
LIB_NAME_FULL = libspawner.so.1
OBJS=spawner.o io.o exec_unix.o
CCFLAGS+=-fpic  -D_REENTRANT -I$(JDK_INCLUDES)

spawner : $(OBJS)
	gcc -g -shared -Wl,-soname,$(LIB_NAME) -o $(LIB_NAME_FULL) $(OBJS) -lc

clean :
	rm $(OBJS)
