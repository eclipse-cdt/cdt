BINDIR = ../bin
BUILDDIR = ../build
SRC_C = $(wildcard *.c)
SRC_CXX = $(wildcard *.cc)

# We rebuild everything whenever one of the headers change
HEADERS = $(wildcard *.h)

# Use .exe extension so that files are named the same way in Linux and Windows.
BINS = $(patsubst %.c,$(BINDIR)/%.exe,$(SRC_C)) $(patsubst %.cc,$(BINDIR)/%.exe,$(SRC_CXX))

CC = gcc
CXX = g++

CFLAGS = -g3 -O0
CXXFLAGS = -g3 -O0

# Don't try to use pthread on Windows
# The OS environment variable exists on Windows
ifneq ($(OS),Windows_NT)
	CFLAGS += -pthread
	CXXFLAGS += -pthread
endif

MKDIR = mkdir -p
RM = rm -f
RMDIR = rmdir

.PHONY: all clean

all: $(BINS)

$(BINDIR):
	$(MKDIR) $@

$(BINDIR)/%.exe: %.c $(HEADERS) Makefile | $(BINDIR)
	$(CC) $(CFLAGS) -o $@ $<

$(BINDIR)/%.exe: %.cc $(HEADERS) Makefile | $(BINDIR)
	$(CXX) $(CXXFLAGS) -o $@ $<

clean:
	$(RM) -r $(BINDIR) $(BUILDDIR)
