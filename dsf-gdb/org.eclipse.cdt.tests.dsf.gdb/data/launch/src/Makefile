src = $(wildcard *.cc *.c)
destDir = ../bin
GCCFLAGS = -g -pthread

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
