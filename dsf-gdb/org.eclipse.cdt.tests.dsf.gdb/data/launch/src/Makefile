BINDIR = ../bin
BUILDDIR = ../build
BUILD2DIR = ../build2
SRC_C = $(wildcard *.c)
SRC_CXX = $(wildcard *.cc)

# We rebuild everything whenever one of the headers change
HEADERS = $(wildcard *.h)

# Use .exe extension so that files are named the same way in Linux and Windows.
BINS = $(patsubst %.c,$(BINDIR)/%.exe,$(SRC_C)) $(patsubst %.cc,$(BINDIR)/%.exe,$(SRC_CXX))
BINS += $(BINDIR)/SourceLookupAC.exe $(BINDIR)/SourceLookupAN.exe $(BINDIR)/SourceLookupRC.exe $(BINDIR)/SourceLookupRN.exe
COREFILE = $(BINDIR)/core

CC = gcc
CXX = g++

# Uncomment the OLDDWARFFLAGS to run newer GCC against older GDB
# As of the time of writing, using GCC 4.8.4:
#  GDB <= 7.1 does not work at all because "Dwarf Error: wrong version in compilation unit header (is 4, should be 2)"
#  GDB <= 7.4 fails some tests because files are not resolved the same way by GDB.
#    With OLDDWARFFLAGS SourceLookupRC returns file="SourceLookup.cc" in frame info even if SourceLookup.cc is not found
#    Without OLDDWARFFLAGS SourceLookupRC the file field in the frame info is left out if SourceLookup.cc is not found
# Therefore for running tests on recent GDBs, OLDDWARFFLAGS should remain unset.
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

$(BUILD2DIR):
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
# This is aiming to represent the different ways that the path to a source
# file can be recorded in debug information. See comments on each one
# for the intention
ABSOLUTE_NONCANONICAL_BUILDDIR=$(shell cd $(CURDIR)/$(BUILD2DIR);pwd)/$(BUILDDIR)
ABSOLUTE_CANONICAL_BUILDDIR=$(shell cd $(CURDIR)/$(BUILDDIR);pwd)
$(BINDIR)/SourceLookupAC.exe $(BINDIR)/SourceLookupAN.exe $(BINDIR)/SourceLookupRC.exe $(BINDIR)/SourceLookupRN.exe: SourceLookup.cc $(HEADERS) Makefile | $(BINDIR) $(BUILDDIR) $(BUILD2DIR)
	cp SourceLookup.cc $(BUILDDIR)/SourceLookup.cc
# Absolute and canonical path
	cd $(BUILDDIR) && $(CXX) $(CXXFLAGS) -o $(BINDIR)/SourceLookupAC.exe $(ABSOLUTE_CANONICAL_BUILDDIR)/SourceLookup.cc
# Absolute and non-canonical path (contains ..)
	cd $(BUILD2DIR) && $(CXX) $(CXXFLAGS) -o $(BINDIR)/SourceLookupAN.exe $(ABSOLUTE_NONCANONICAL_BUILDDIR)/SourceLookup.cc
# Relative and canonical path
	cd $(BUILDDIR) && $(CXX) $(CXXFLAGS) -o $(BINDIR)/SourceLookupRC.exe SourceLookup.cc
# Relative and non-canonical path (contains ..)
	cd $(BUILD2DIR) && $(CXX) $(CXXFLAGS) -o $(BINDIR)/SourceLookupRN.exe $(BUILDDIR)/SourceLookup.cc
	rm $(BUILDDIR)/SourceLookup.cc

clean:
	$(RM) -r $(BINDIR) $(BUILDDIR) $(BUILD2DIR)
