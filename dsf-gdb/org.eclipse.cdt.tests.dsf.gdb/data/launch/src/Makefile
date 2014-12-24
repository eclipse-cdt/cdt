BINDIR = ../bin
SRC_C = $(wildcard *.c)
SRC_CXX = $(wildcard *.cc)

# We rebuild everything whenever one of the headers change
HEADERS = $(wildcard *.h)

# Use .exe extension so that files are named the same way in Linux and Windows.
BINS = $(patsubst %.c,$(BINDIR)/%.exe,$(SRC_C)) $(patsubst %.cc,$(BINDIR)/%.exe,$(SRC_CXX))
COREFILE = $(BINDIR)/core

CC = gcc
CXX = g++

CFLAGS = -g
CXXFLAGS = -g

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

$(BINDIR)/%.exe: %.c $(HEADERS) | $(BINDIR)
	$(CC) $(CFLAGS) -o $@ $<

$(BINDIR)/%.exe: %.cc $(HEADERS) | $(BINDIR)
	$(CXX) $(CXXFLAGS) -o $@ $<

# Generate a core file that is needed for post-morted core-file tests
$(COREFILE): $(BINDIR)/ExpressionTestApp.exe | $(BINDIR)
	gdb -nx --batch -ex 'b testLocals' -ex 'run' --ex 'next 16' \
		-ex 'gcore ../bin/core' $(BINDIR)/ExpressionTestApp.exe > /dev/null

clean:
	$(RM) -r $(BINDIR)
