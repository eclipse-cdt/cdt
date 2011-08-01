OS = win32
ARCH = x86

TARGET = $(INSTALL_DIR)/starter.exe

OBJS = starter.o

LIBS = -lpsapi

CXX = g++
CXXFLAGS = -DUNICODE

INSTALL_DIR = ../../../org.eclipse.cdt.core.$(OS).$(ARCH)/os/$(OS)/$(ARCH)

all: $(TARGET)

rebuild: clean all

clean :
	$(RM) $(OBJS)

$(TARGET) : $(OBJS)
	$(CXX) -o $(TARGET) $(OBJS) $(LIBS)
