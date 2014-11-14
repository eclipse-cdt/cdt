BINDIR = ../bin
SRC_C = $(wildcard *.c)
SRC_CXX = $(wildcard *.cc)

# Use .exe extension so that files are named the same way in Linux and Windows.
BINS = $(patsubst %.c,$(BINDIR)/%.exe,$(SRC_C)) $(patsubst %.cc,$(BINDIR)/%.exe,$(SRC_CXX))
COREFILE = $(BINDIR)/core

CC = gcc
CFLAGS = -gdwarf-2 -pthread

CXX = g++
CXXFLAGS = -gdwarf-2 -pthread

MKDIR = mkdir -p
RM = rm -f
RMDIR = rmdir

.PHONY: all clean

all: $(BINS) $(COREFILE)

$(BINDIR):
	$(MKDIR) $@

$(BINDIR)/%.exe: %.c | $(BINDIR)
	$(CC) $(CFLAGS) -o $@ $<

$(BINDIR)/%.exe: %.cc | $(BINDIR)
	$(CXX) $(CXXFLAGS) -o $@ $<

# Generate a core file that is needed for post-morted core-file tests
$(COREFILE): $(BINDIR)/ExpressionTestApp.exe | $(BINDIR)
	gdb -nx --batch -ex 'b testLocals' -ex 'run' --ex 'next 16' \
		-ex 'gcore ../bin/core' $(BINDIR)/ExpressionTestApp.exe > /dev/null

clean:
	$(RM) -r $(BINDIR)
