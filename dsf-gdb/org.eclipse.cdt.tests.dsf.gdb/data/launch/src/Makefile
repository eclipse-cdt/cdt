BINDIR = ../bin
SRC_C = $(wildcard *.c)
SRC_CXX = $(wildcard *.cc)

# Use .exe extension so that files are named the same way in Linux and Windows.
BINS = $(patsubst %.c,$(BINDIR)/%.exe,$(SRC_C)) $(patsubst %.cc,$(BINDIR)/%.exe,$(SRC_CXX))

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

$(BINDIR)/%.exe: %.c
	$(CC) $(CFLAGS) -o $@ $<

$(BINDIR)/%.exe: %.cc
	$(CXX) $(CXXFLAGS) -o $@ $<

clean:
	$(RM) $(BINS)
	$(RM) -r $(BINDIR)
