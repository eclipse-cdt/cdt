BINDIR = ../bin
BUILDDIR = ../build
SRC_C = $(wildcard *.c)
SRC_CXX = $(wildcard *.cc)

# We rebuild everything whenever one of the headers change
HEADERS = $(wildcard *.h)

# Use .exe extension so that files are named the same way in Linux and Windows.
BINS = $(patsubst %.c,$(BINDIR)/%.exe,$(SRC_C)) $(patsubst %.cc,$(BINDIR)/%.exe,$(SRC_CXX))
COREFILE = $(BINDIR)/core

CC = gcc
CXX = g++

# Uncomment the OLDDWARFFLAGS to run newer GCC against older GDB
# OLDDWARFFLAGS =  -gdwarf-2 -gstrict-dwarf
CFLAGS = -g3 -O0 $(OLDDWARFFLAGS)
CXXFLAGS = -g3 -O0  $(OLDDWARFFLAGS)

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

all: $(BINS) $(COREFILE)

$(BINDIR):
	$(MKDIR) $@

$(BUILDDIR):
	$(MKDIR) $@

$(BINDIR)/%.exe: %.c $(HEADERS) Makefile | $(BINDIR)
	$(CC) $(CFLAGS) -o $@ $<

$(BINDIR)/%.exe: %.cc $(HEADERS) Makefile | $(BINDIR)
	$(CXX) $(CXXFLAGS) -o $@ $<

# Generate a core file that is needed for post-morted core-file tests
$(COREFILE): $(BINDIR)/ExpressionTestApp.exe Makefile | $(BINDIR)
	gdb -nx --batch -ex 'b testLocals' -ex 'run' --ex 'next 16' \
		-ex 'gcore ../bin/core' $(BINDIR)/ExpressionTestApp.exe > /dev/null

# Compile a source file from an alternate directory
$(BINDIR)/SourceLookup.exe: SourceLookup.cc $(HEADERS) Makefile | $(BINDIR) $(BUILDDIR)
	cp SourceLookup.cc $(BUILDDIR)/SourceLookup.cc
	cd $(BUILDDIR) && $(CXX) $(CXXFLAGS) -o $@ SourceLookup.cc
	rm $(BUILDDIR)/SourceLookup.cc

clean:
	$(RM) -r $(BINDIR)
