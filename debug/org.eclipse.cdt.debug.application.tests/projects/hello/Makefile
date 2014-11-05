all: a.out

CFLAGS=-I. -g3 -O2 -Djeff=2

a.out: hello.o x.o
	gcc -g -o a.out $?

hello.o: hello.c somehdr.h
	gcc -c $(CFLAGS) hello.c

x.o: x.c
	gcc -c $(CFLAGS) $<

clean:
	rm *.o *.out
