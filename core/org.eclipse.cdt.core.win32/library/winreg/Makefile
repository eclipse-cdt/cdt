ifeq ($(JAVA_HOME),)
$(warning JAVA_HOME not set in environment)
endif

# Defaults which can be overridden.
OS = win32
ARCH = x86

JDK_INCLUDES = "$(JAVA_HOME)/include"
JDK_OS_INCLUDES = "$(JAVA_HOME)/include/$(OS)"

CXX = g++
CXXFLAGS = -DUNICODE -I$(JDK_INCLUDES) -I$(JDK_OS_INCLUDES)

INSTALL_DIR = ../../os/$(OS)/$(ARCH)

TARGET = $(INSTALL_DIR)/winreg.dll
OBJS = winreg.o

all: $(TARGET)

rebuild: clean all

clean :
	$(RM) $(OBJS)

$(TARGET) : $(OBJS)
	$(CXX) -Wl,--kill-at -shared -o $(TARGET) $(OBJS)
