ifeq ($(JAVA_HOME),)
$(error JAVA_HOME not set in environment)
endif

TARGET = $(INSTALL_DIR)/winreg.dll

OBJS = winreg.o

OS = win32
ARCH = x86

JDK_INCLUDES = "$(JAVA_HOME)/include"
JDK_OS_INCLUDES = "$(JAVA_HOME)/include/$(OS)"

CXX = g++
CXXFLAGS = -DUNICODE -I$(JDK_INCLUDES) -I$(JDK_OS_INCLUDES)

INSTALL_DIR = ../../../org.eclipse.cdt.core.$(OS).$(ARCH)/os/$(OS)/$(ARCH)

all: $(TARGET)

rebuild: clean all

clean :
	$(RM) $(OBJS)

$(TARGET) : $(OBJS)
	$(CXX) -Wl,--kill-at -shared -o $(TARGET) $(OBJS)
