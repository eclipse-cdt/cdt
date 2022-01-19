################################################################################
# Automatically-generated file. Do not edit!
################################################################################

-include ../makefile.init

RM := rm -rf

# All of the sources participating in the build are defined here
-include sources.mk
-include subdir.mk

ifneq ($(MAKECMDGOALS),clean)
ifneq ($(strip $(C_DEPS)),)
-include $(C_DEPS)
endif
endif

-include ../makefile.defs

OPTIONAL_TOOL_DEPS := \
$(wildcard ../makefile.defs) \
$(wildcard ../makefile.init) \
$(wildcard ../makefile.targets) \


BUILD_ARTIFACT_NAME := Test30_1
BUILD_ARTIFACT_EXTENSION := so.4.5.6
BUILD_ARTIFACT_PREFIX :=
BUILD_ARTIFACT := $(BUILD_ARTIFACT_PREFIX)$(BUILD_ARTIFACT_NAME)$(if $(BUILD_ARTIFACT_EXTENSION),.$(BUILD_ARTIFACT_EXTENSION),)

# Add inputs and outputs from these tool invocations to the build variables 
TEST30_1_GNU_SO_DEPLOY_OUTPUT_OUTPUTS += \
default.so.4.5.6 \

EXECUTABLES += \
test30_1.so.1.2.3 \


# All Target
all: main-build

# Main-build Target
main-build: Test30_1.so.4.5.6 secondary-outputs

# Tool invocations
Test30_1.so.4.5.6: $(EXECUTABLES) makefile $(OPTIONAL_TOOL_DEPS)
	@echo 'Building target: $@'
	@echo 'Invoking: Deploy tool'
	cp $(EXECUTABLES) "Test30_1.so.4.5.6"
	@echo 'Finished building target: $@'
	@echo ' '

default.so.4.5.6: Test30_1.so.4.5.6 $(EXECUTABLES)

test30_1.so.1.2.3: $(STRIPPEDOBJS) makefile $(OPTIONAL_TOOL_DEPS)
	@echo 'Invoking: MBS30.linker.gnu.c'
	gcc -shared -Wl,-soname=test30_1.so.1.2.3 -o "test30_1.so.1.2.3" $(STRIPPEDOBJS)
	@echo 'Finished building: $@'
	@echo ' '

# Other Targets
clean:
	-$(RM) Test30_1.so.4.5.6 default.so.4.5.6 test30_1.so.1.2.3
	-@echo ' '

secondary-outputs: $(TEST30_1_GNU_SO_CJPEG_OUTPUT_OUTPUTS)

.PHONY: all clean dependents main-build

-include ../makefile.targets
