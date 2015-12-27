BINDIR = ../bin
BUILDDIR = ../build
BUILD2DIR = ../build2
SRC_C = $(wildcard *.c)
SRC_CXX = $(wildcard *.cc)

# We rebuild everything whenever one of the headers change
HEADERS = $(wildcard *.h)

# These Source Lookup tests compile in a different directory than the source
# is in to require source lookup is in.
SOURCELOOKUP_BINS = $(BINDIR)/SourceLookupAC.exe $(BINDIR)/SourceLookupAN.exe $(BINDIR)/SourceLookupRC.exe $(BINDIR)/SourceLookupRN.exe
# These tests are for running against older GDBs which require compatibilty flags
# for GCC debug info, see OLDDWARFFLAGS below
SOURCELOOKUP_BINS += $(BINDIR)/SourceLookupDwarf2AC.exe $(BINDIR)/SourceLookupDwarf2AN.exe $(BINDIR)/SourceLookupDwarf2RC.exe $(BINDIR)/SourceLookupDwarf2RN.exe 

# Use .exe extension so that files are named the same way in Linux and Windows.
BINS = $(patsubst %.c,$(BINDIR)/%.exe,$(SRC_C)) $(patsubst %.cc,$(BINDIR)/%.exe,$(SRC_CXX))
BINS += $(SOURCELOOKUP_BINS)
# This source lookup exe (like the default SourceLookup.exe) compiles from non-relocated source.
BINS += $(BINDIR)/SourceLookupDwarf2.exe
COREFILE = $(BINDIR)/core

CC = gcc
CXX = g++

# As of the time of writing, using GCC 4.8.4 OLDDWARFFLAGS is required:
#  for -> because:
#  GDB <= 7.1 -> does not work at all because "Dwarf Error: wrong version in compilation unit header (is 4, should be 2)"
#  GDB <= 7.4 -> fails some tests because files are not resolved the same way by GDB.
#    With OLDDWARFFLAGS SourceLookupRC returns file="SourceLookup.cc" in frame info even if SourceLookup.cc is not found
#    Without OLDDWARFFLAGS SourceLookupRC the file field in the frame info is left out if SourceLookup.cc is not found
# For running tests on recent GDBs (> 7.5), OLDDWARFFLAGS should remain unset.
OLDDWARFFLAGS =  -gdwarf-2 -gstrict-dwarf
# By default only SourceLookup tests take into account OLDDWARFFLAGS, if testing any other tests
# with older GDBs, adding OLDDWARFFLAGS to the compilation flags is necessary.
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

# Compile sourcelookup once with old dwarf flags
$(BINDIR)/SourceLookupDwarf2.exe: SourceLookup.cc $(HEADERS) Makefile | $(BINDIR)
	$(CXX) $(CXXFLAGS) $(OLDDWARFFLAGS) -o $@ $<

# Compile a source file from an alternate directory
# This is aiming to represent the different ways that the path to a source
# file can be recorded in debug information. See comments on each one
# for the intention
ABSOLUTE_NONCANONICAL_BUILDDIR=$(shell cd $(CURDIR)/$(BUILD2DIR);pwd)/$(BUILDDIR)
ABSOLUTE_CANONICAL_BUILDDIR=$(shell cd $(CURDIR)/$(BUILDDIR);pwd)
$(SOURCELOOKUP_BINS): SourceLookup.cc $(HEADERS) Makefile | $(BINDIR) $(BUILDDIR) $(BUILD2DIR)
	cp SourceLookup.cc $(BUILDDIR)/SourceLookup.cc
# Absolute and canonical path
	cd $(BUILDDIR) && $(CXX) $(CXXFLAGS) -o $(BINDIR)/SourceLookupAC.exe $(ABSOLUTE_CANONICAL_BUILDDIR)/SourceLookup.cc
	cd $(BUILDDIR) && $(CXX) $(CXXFLAGS) $(OLDDWARFFLAGS) -o $(BINDIR)/SourceLookupDwarf2AC.exe $(ABSOLUTE_CANONICAL_BUILDDIR)/SourceLookup.cc
# Absolute and non-canonical path (contains ..)
	cd $(BUILD2DIR) && $(CXX) $(CXXFLAGS) -o $(BINDIR)/SourceLookupAN.exe $(ABSOLUTE_NONCANONICAL_BUILDDIR)/SourceLookup.cc
	cd $(BUILD2DIR) && $(CXX) $(CXXFLAGS) $(OLDDWARFFLAGS) -o $(BINDIR)/SourceLookupDwarf2AN.exe $(ABSOLUTE_NONCANONICAL_BUILDDIR)/SourceLookup.cc
# Relative and canonical path
	cd $(BUILDDIR) && $(CXX) $(CXXFLAGS) -o $(BINDIR)/SourceLookupRC.exe SourceLookup.cc
	cd $(BUILDDIR) && $(CXX) $(CXXFLAGS) $(OLDDWARFFLAGS) -o $(BINDIR)/SourceLookupDwarf2RC.exe SourceLookup.cc
# Relative and non-canonical path (contains ..)
	cd $(BUILD2DIR) && $(CXX) $(CXXFLAGS) -o $(BINDIR)/SourceLookupRN.exe $(BUILDDIR)/SourceLookup.cc
	cd $(BUILD2DIR) && $(CXX) $(CXXFLAGS) $(OLDDWARFFLAGS) -o $(BINDIR)/SourceLookupDwarf2RRN.exe $(BUILDDIR)/SourceLookup.cc

# cleanup
	rm $(BUILDDIR)/SourceLookup.cc

clean:
	$(RM) -r $(BINDIR) $(BUILDDIR) $(BUILD2DIR)
