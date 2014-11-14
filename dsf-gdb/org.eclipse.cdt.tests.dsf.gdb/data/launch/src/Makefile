BINDIR = ../bin
SRC_C = $(wildcard *.c)
SRC_CXX = $(wildcard *.cc)
BINS = $(patsubst %.c,$(BINDIR)/%,$(SRC_C)) $(patsubst %.cc,$(BINDIR)/%,$(SRC_CXX))

CC = gcc
CFLAGS = -gdwarf-2 -pthread

CXX = g++
CXXFLAGS = -gdwarf-2 -pthread

MKDIR = mkdir -p
RM = rm -f
RMDIR = rmdir

.PHONY: all clean

all: $(BINDIR) $(BINS)

$(BINDIR):
	$(MKDIR) $@

$(BINDIR)/%: %.c
	$(CC) $(CFLAGS) -o $@ $<

$(BINDIR)/%: %.cc
	$(CXX) $(CXXFLAGS) -o $@ $<

clean:
	$(RM) $(BINS)
	$(RM) -r $(BINDIR)
