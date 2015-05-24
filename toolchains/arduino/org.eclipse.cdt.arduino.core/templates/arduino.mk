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

VERSION = 164

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

space :=
space +=
spacify = $(subst $(space),\$(space),$1)

ifeq ($(OS),Windows_NT)
RMDIR = rmdir /s /q
fixpath = $(subst /,\,$1)
mymkdir = if not exist "$(call fixpath,$1)" mkdir $(call fixpath,$1)
else
RMDIR = rm -fr
fixpath = $1
mymkdir = mkdir -p $1
endif

src_recurse = $(foreach d,$(subst $2/,,$(wildcard $1*)),$(call src_recurse,$3/$d/,$2,$3) $(filter %.c %.cpp,$d))
src = $(foreach lib,$3,$(if $(wildcard $2/$(lib)/src),$(call src_recurse,$2/$(lib)/src/,$1,$2),\
	$(subst $1/,,\
		$(wildcard $2/$(lib)/*.c)\
		$(wildcard $2/$(lib)/*.cpp)\
		$(wildcard $2/$(lib)/utility/*.c)\
		$(wildcard $2/$(lib)/utility/*.cpp)))))
objs = $(patsubst %.c,$2/%.o,$(filter %.c,$1)) $(patsubst %.cpp,$2/%.o,$(filter %.cpp,$1))
incs = $(foreach lib,$1,$(if $(wildcard $3/$(lib)/src),-I"$2/$(lib)/src",-I"$2/$(lib)" -I"$2/$(lib)/utility"))

PROJECT_OBJS = $(call objs,$(call src_recurse,./,.,.),$(OUTPUT_DIR)/src)

LIB_ROOT = $(ARDUINO_HOME)/hardware/arduino/$(ARCH)/cores/$(BUILD_CORE)
LIB_ROOT_SPC = $(call spacify,$(LIB_ROOT))
LIB_ROOT_SPC2 = $(subst :,\:,$(subst \,\\\,$(LIB_ROOT_SPC)))
LIB_OBJS = $(call objs,$(call src_recurse,$(LIB_ROOT_SPC)/,$(LIB_ROOT),$(LIB_ROOT_SPC)),$(OUTPUT_DIR)/lib)

USER_LIB_ROOT = $(ARDUINO_USER_LIBS)
USER_LIB_ROOT_SPC = $(call spacify,$(USER_LIB_ROOT))
USER_LIB_ROOT_SPC2 = $(subst :,\:,$(subst \,\\\,$(USER_LIB_ROOT_SPC)))
USER_LIBS = $(foreach lib,$(LIBS),$(subst $(USER_LIB_ROOT)/,,$(wildcard $(USER_LIB_ROOT_SPC)/$(lib))))
USER_INCLUDES = $(call incs,$(USER_LIBS),$(USER_LIB_ROOT),$(USER_LIB_ROOT_SPC))
USER_OBJS = $(call objs,$(call src,$(USER_LIB_ROOT),$(USER_LIB_ROOT_SPC),$(USER_LIBS)),$(OUTPUT_DIR)/user)

HW_LIB_ROOT = $(ARDUINO_HOME)/hardware/arduino/$(ARCH)/libraries
HW_LIB_ROOT_SPC = $(call spacify,$(HW_LIB_ROOT))
HW_LIB_ROOT_SPC2 = $(subst :,\:,$(subst \,\\\,$(HW_LIB_ROOT_SPC)))
HW_LIBS = $(foreach lib, $(LIBS), $(subst $(HW_LIB_ROOT)/,,$(wildcard $(HW_LIB_ROOT_SPC)/$(lib))))
HW_INCLUDES = $(call incs,$(HW_LIBS),$(HW_LIB_ROOT),$(HW_LIB_ROOT_SPC))
HW_OBJS = $(call objs,$(call src,$(HW_LIB_ROOT),$(HW_LIB_ROOT_SPC),$(HW_LIBS)),$(OUTPUT_DIR)/hw)

ARDUINO_LIB_ROOT = $(ARDUINO_HOME)/libraries
ARDUINO_LIB_ROOT_SPC = $(call spacify,$(ARDUINO_LIB_ROOT))
ARDUINO_LIB_ROOT_SPC2 = $(subst :,\:,$(subst \,\\\,$(ARDUINO_LIB_ROOT_SPC)))
ARDUINO_LIBS = $(foreach lib, $(LIBS), $(subst $(ARDUINO_LIB_ROOT)/,,$(wildcard $(ARDUINO_LIB_ROOT_SPC)/$(lib))))
ARDUINO_INCLUDES = $(call incs,$(ARDUINO_LIBS),$(ARDUINO_LIB_ROOT),$(ARDUINO_LIB_ROOT_SPC))
ARDUINO_OBJS = $(call objs,$(call src,$(ARDUINO_LIB_ROOT),$(ARDUINO_LIB_ROOT_SPC),$(ARDUINO_LIBS)),$(OUTPUT_DIR)/arduino)

INCLUDES = -I"$(ARDUINO_HOME)/hardware/arduino/$(ARCH)/cores/$(BUILD_CORE)" \
           -I"$(ARDUINO_HOME)/hardware/arduino/$(ARCH)/variants/$(BUILD_VARIANT)" \
           $(USER_INCLUDES) $(HW_INCLUDES) $(ARDUINO_INCLUDES)

OBJS = $(PROJECT_OBJS) $(USER_OBJS) $(HW_OBJS) $(ARDUINO_OBJS)

all:	$(OUTPUT_DIR)/$(EXE).hex

clean:
	$(RMDIR) $(call fixpath,$(OUTPUT_DIR))

load:	$(OUTPUT_DIR)/$(EXE).hex
	$(do_load_$(LOADER))

$(OUTPUT_DIR)/$(EXE).hex:	$(OBJS) $(OUTPUT_DIR)/core.a
	$(do_link)

$(OUTPUT_DIR)/core.a: $(LIB_OBJS)
	$(AR) r $@ $?

$(OUTPUT_DIR)/lib/%.o: $(LIB_ROOT_SPC2)/%.c
	@-$(call mymkdir,$(dir $@))
	$(CC) -c $(CFLAGS) $(CPPFLAGS) -o $@ "$<" 

$(OUTPUT_DIR)/lib/%.o: $(LIB_ROOT_SPC2)/%.cpp
	@-$(call mymkdir,$(dir $@))
	$(CXX) -c $(CXXFLAGS) $(CPPFLAGS) -o $@ "$<" 

$(OUTPUT_DIR)/user/%.o: $(USER_LIB_ROOT_SPC2)/%.c
	@-$(call mymkdir,$(dir $@))
	$(CC) -c $(CFLAGS) $(CPPFLAGS) -o $@ "$<" 

$(OUTPUT_DIR)/user/%.o: $(USER_LIB_ROOT_SPC2)/%.cpp
	@-$(call mymkdir,$(dir $@))
	$(CXX) -c $(CXXFLAGS) $(CPPFLAGS) -o $@ "$<" 

$(OUTPUT_DIR)/hw/%.o: $(HW_LIB_ROOT_SPC2)/%.c
	@-$(call mymkdir,$(dir $@))
	$(CC) -c $(CFLAGS) $(CPPFLAGS) -o $@ "$<" 

$(OUTPUT_DIR)/hw/%.o: $(HW_LIB_ROOT_SPC2)/%.cpp
	@-$(call mymkdir,$(dir $@))
	$(CXX) -c $(CXXFLAGS) $(CPPFLAGS) -o $@ "$<" 

$(OUTPUT_DIR)/arduino/%.o: $(ARDUINO_LIB_ROOT_SPC2)/%.c
	@-$(call mymkdir,$(dir $@))
	$(CC) -c $(CFLAGS) $(CPPFLAGS) -o $@ "$<" 

$(OUTPUT_DIR)/arduino/%.o: $(ARDUINO_LIB_ROOT_SPC2)/%.cpp
	@-$(call mymkdir,$(dir $@))
	$(CXX) -c $(CXXFLAGS) $(CPPFLAGS) -o $@ "$<" 

$(OUTPUT_DIR)/src/%.o: %.c
	@-$(call mymkdir,$(dir $@))
	$(CC) -c $(CFLAGS) $(CPPFLAGS) -o $@ $< 

$(OUTPUT_DIR)/src/%.o: %.cpp
	@-$(call mymkdir,$(dir $@))
	$(CXX) -c $(CXXFLAGS) $(CPPFLAGS) -o $@ $< 
