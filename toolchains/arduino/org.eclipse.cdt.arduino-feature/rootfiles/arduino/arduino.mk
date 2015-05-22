VERSION = 156

BOARD ?= uno
OUTPUT_DIR ?= build/Default

space :=
space +=
spacify = $(subst $(space),\$(space),$1)
rwildcard = $(foreach d,$(wildcard $1*),$(call rwildcard,$(call spacify,$d)/,$2) $(filter $(subst *,%,$2),$d))

ifeq ($(OS),Windows_NT)
RMDIR = rmdir /s /q
fixpath = $(subst /,\,$1)
mymkdir = if not exist "$(call fixpath,$1)" mkdir $(call fixpath,$1)
else
RMDIR = rm -fr
fixpath = $1
mymkdir = mkdir -p $1
endif

ifeq ($(BOARD),uno)
ARCH = avr
BUILD_CORE = arduino
BUILD_VARIANT = standard
BUILD_MCU = atmega328p
BUILD_F_CPU = 16000000L
BUILD_BOARD = AVR_UNO
LOADER = avrdude
LOADER_PROTOCOL = arduino
LOADER_SPEED = 115200
LOADER_MAX_SIZE = 32256
LOADER_MAX_DATA = 2048
endif

ifeq ($(ARCH),avr)
CXXFLAGS = -g -Os -w -fno-exceptions -ffunction-sections -fdata-sections -MMD \
	-mmcu=$(BUILD_MCU) -DF_CPU=$(BUILD_F_CPU) -DARDUINO=$(VERSION) -DARDUINO_$(BUILD_BOARD) -DARDUINO_ARCH_AVR $(INCLUDES)
CFLAGS = -g -Os -w -ffunction-sections -fdata-sections -MMD \
	-mmcu=$(BUILD_MCU) -DF_CPU=$(BUILD_F_CPU) -DARDUINO=156 -DARDUINO_$(BUILD_BOARD) -DARDUINO_ARCH_AVR $(INCLUDES)

CXX = avr-g++
CC = avr-gcc
AR = avr-ar
OBJCOPY = avr-objcopy

define do_link
$(CC) -Os -Wl,--gc-sections -mmcu=$(BUILD_MCU) -o $(OUTPUT_DIR)/$(EXE).elf $^
avr-objcopy -O ihex -R .eeprom $(OUTPUT_DIR)/$(EXE).elf $(OUTPUT_DIR)/$(EXE).hex
$(do_link_extra)
avr-size $(OUTPUT_DIR)/$(EXE).elf
@echo Max text: $(LOADER_MAX_SIZE)
@echo Max data + bss: $(LOADER_MAX_DATA)
endef

define do_eeprom
avr-objcopy -O ihex -j .eeprom --set-section-flags=.eeprom=alloc,load \
	--no-change-warnings --change-section-lma .eeprom=0 \
	$(OUTPUT_DIR)/$(EXE).elf  $(OUTPUT_DIR)/$(EXE).eep
endef

define do_load_avrdude
avrdude -C"$(ARDUINO_HOME)/hardware/tools/avr/etc/avrdude.conf" -p$(BUILD_MCU) -c$(LOADER_PROTOCOL) \
	-P$(SERIAL_PORT) -b$(LOADER_SPEED) -D "-Uflash:w:$(OUTPUT_DIR)/$(EXE).hex:i"
endef

endif # ARCH = avr

LIB_ROOT = $(ARDUINO_HOME)/hardware/arduino/$(ARCH)/cores/$(BUILD_CORE)

LIB_SRCS = $(wildcard $(call spacify,$(LIB_ROOT))/*.c) \
           $(wildcard $(call spacify,$(LIB_ROOT))/*.cpp)

LIB_OBJS = $(patsubst $(call spacify,$(LIB_ROOT))/%.c, $(OUTPUT_DIR)/arduino/%.o, $(filter %.c,$(LIB_SRCS))) \
           $(patsubst $(call spacify,$(LIB_ROOT))/%.cpp, $(OUTPUT_DIR)/arduino/%.o, $(filter %.cpp,$(LIB_SRCS)))

$(info LIB_SRCS = $(LIB_SRCS))
$(info LIB_OBJS = $(LIB_OBJS))

ifdef OFF
LIBS_ROOTS = $(ARDUINO_LIBS) $(ARDUINO_HOME)/hardware/arduino/$(ARCH)/libraries \
			 $(ARDUINO_HOME)/libraries

LIBS_DIRS = $(foreach lib, $(LIBS), $(firstword $(realpath $(foreach lib_root, $(LIBS_ROOTS), $(lib_root)/$(lib)))))
endif

INCLUDES = -I"$(ARDUINO_HOME)/hardware/arduino/$(ARCH)/cores/$(BUILD_CORE)" \
           -I"$(ARDUINO_HOME)/hardware/arduino/$(ARCH)/variants/$(BUILD_VARIANT)" \
           $(foreach lib, $(LIBS_DIRS), -I"$(lib)")

SRCS = $(call rwildcard, ./, *.c *.cpp) $(foreach lib, $(LIBS_DIRS), $(wildcard $(lib)/*.c $(lib)/*.cpp $(lib)/utility/*.c $(lib)/utility/*.cpp))

OBJS = $(patsubst %.cpp, $(OUTPUT_DIR)/%.o, $(filter %.cpp, $(SRCS))) \
       $(patsubst %.c, $(OUTPUT_DIR)/%.o, $(filter %.c, $(SRCS)))

all:	$(OUTPUT_DIR)/$(EXE).hex

clean:
	$(RMDIR) $(call fixpath,$(OUTPUT_DIR))

load:	#$(OUTPUT_DIR)/$(EXE).hex
	$(do_load_$(LOADER))

$(OUTPUT_DIR)/$(EXE).hex:	$(OBJS) $(OUTPUT_DIR)/core.a
	$(do_link)

$(OUTPUT_DIR)/core.a: $(LIB_OBJS)
	$(AR) r $@ $?

$(OUTPUT_DIR)/arduino/%.o: $(LIB_ROOT)/%.c
	@-$(call mymkdir,$(dir $@))
	$(CC) -c $(CFLAGS) $(CPPFLAGS) -o $@ $< 

$(OUTPUT_DIR)/arduino/%.o: $(LIB_ROOT)/%.cpp
	@-$(call mymkdir,$(dir $@))
	$(CXX) -c $(CXXFLAGS) $(CPPFLAGS) -o $@ $< 

$(OUTPUT_DIR)/%.o: %.c
	@-$(call mymkdir,$(dir $@))
	$(CC) -c $(CFLAGS) $(CPPFLAGS) -o $@ $< 

$(OUTPUT_DIR)/%.o: %.cpp
	@-$(call mymkdir,$(dir $@))
	$(CXX) -c $(CXXFLAGS) $(CPPFLAGS) -o $@ $< 
