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


# All Target
all: test30_2.tmp

# Tool invocations
test30_2.tmp: $(LOGFILE) makefile $(OPTIONAL_TOOL_DEPS)
	@echo 'Building target: $@'
	@echo 'Invoking: cat'
	cat $(LOGFILE)
	@echo 'Finished building target: $@'
	@echo ' '

new.tar: $(CPP_SRCS) $(ASM_SRCS) makefile $(OPTIONAL_TOOL_DEPS)
	@echo 'Invoking: tar-create'
	tar -cf new.tar $(CPP_SRCS) $(ASM_SRCS)
	@echo 'Finished building: $@'
	@echo ' '

new.log: $(TAROUT) makefile $(OPTIONAL_TOOL_DEPS)
	@echo 'Invoking: tar-list'
	tar -tf $(TAROUT) > new.log
	@echo 'Finished building: $@'
	@echo ' '

# Other Targets
clean:
	-$(RM) $(TAROUT)$(TERMINAL__DUMMY_OUTPUT__OUTPUTS)$(LOGFILE) test30_2.tmp
	-@echo ' '

.PHONY: all clean dependents

-include ../makefile.targets
