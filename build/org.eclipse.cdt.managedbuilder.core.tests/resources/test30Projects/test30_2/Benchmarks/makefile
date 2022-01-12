################################################################################
# Automatically-generated file. Do not edit!
################################################################################

-include ../makefile.init

RM := rm -rf

# All of the sources participating in the build are defined here
-include sources.mk
-include ABC/subdir.mk
-include subdir.mk
-include objects.mk

-include ../makefile.defs

OPTIONAL_TOOL_DEPS := \
$(wildcard ../makefile.defs) \
$(wildcard ../makefile.init) \
$(wildcard ../makefile.targets) \


BUILD_ARTIFACT_NAME := test30_2
BUILD_ARTIFACT_EXTENSION := tmp
BUILD_ARTIFACT_PREFIX :=
BUILD_ARTIFACT := $(BUILD_ARTIFACT_PREFIX)$(BUILD_ARTIFACT_NAME)$(if $(BUILD_ARTIFACT_EXTENSION),.$(BUILD_ARTIFACT_EXTENSION),)

# Add inputs and outputs from these tool invocations to the build variables 
TAROUT += \
new.tar \

LOGFILE += \
new.log \

TOUCHEDFILES += \
alphabetical \
are \
file\ names \
in \
not \
order \
these \


# All Target
all: main-build

# Main-build Target
main-build: test30_2.tmp

# Tool invocations
test30_2.tmp: $(LOGFILE) makefile objects.mk $(OPTIONAL_TOOL_DEPS)
	@echo 'Building target: $@'
	@echo 'Invoking: cat'
	cat $(LOGFILE)
	@echo 'Finished building target: $@'
	@echo ' '

new.tar: $(CPP_SRCS) $(ASM_SRCS) makefile objects.mk $(OPTIONAL_TOOL_DEPS)
	@echo 'Invoking: tar-create'
	tar -cf new.tar $(CPP_SRCS) $(ASM_SRCS)
	@echo 'Finished building: $@'
	@echo ' '

new.log: $(TAROUT) makefile objects.mk $(OPTIONAL_TOOL_DEPS)
	@echo 'Invoking: tar-list'
	tar -tf $(TAROUT) > new.log
	@echo 'Finished building: $@'
	@echo ' '

these file\ names are not in alphabetical order: $(LOGFILE) makefile objects.mk $(OPTIONAL_TOOL_DEPS)
	@echo 'Invoking: touch'
	touch -r $(LOGFILE) "these" "file names" "are" "not" "in" "alphabetical" "order"
	@echo 'Finished building: $@'
	@echo ' '

# Other Targets
clean:
	-$(RM) alphabetical are file\ names in new.log new.tar not order test30_2.tmp these
	-@echo ' '

.PHONY: all clean dependents main-build

-include ../makefile.targets
