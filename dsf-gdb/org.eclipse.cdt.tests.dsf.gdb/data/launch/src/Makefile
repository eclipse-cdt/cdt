src = $(wildcard *.cc *.c)
destDir = ../bin
GCCFLAGS = -gdwarf-2 -pthread -m32

all:
	@mkdir -p $(destDir)
# Name the target with an .exe extension so that CVS does not
# include it when making a patch
	@for file in $(src) ; \
	do \
		target=`basename $$file .c` ; \
		target=`basename $$target .cc` ; \
		g++ $(GCCFLAGS) $$file -o $(destDir)/$$target.exe ; \
	done
