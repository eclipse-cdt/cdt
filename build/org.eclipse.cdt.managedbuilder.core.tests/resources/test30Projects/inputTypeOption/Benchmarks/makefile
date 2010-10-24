################################################################################
# Automatically-generated file. Do not edit!
################################################################################

-include ../makefile.init

RM := rm -rf

# All of the sources participating in the build are defined here
-include sources.mk
-include subdir.mk
-include objects.mk

ifneq ($(MAKECMDGOALS),clean)
ifneq ($(strip $(C_DEPS)),)
-include $(C_DEPS)
endif
endif

-include ../makefile.defs

# Add inputs and outputs from these tool invocations to the build variables 

# All Target
all: inputTypeOption

# Tool invocations
inputTypeOption: $(OBJS) C:\An\ Absolute\ Path\ With\ Spaces\foo.linker $(USER_OBJS)
	@echo 'Building target: $@'
	@echo 'Invoking: org.eclipse.cdt.managedbuilder.core.tests.inputTypeOptionMakefileRenderingTest.c.linker'
	gcc --linkerInputTypeOption="C:\An Absolute Path With Spaces\foo.linker" -o "inputTypeOption" $(OBJS) $(USER_OBJS) $(LIBS)
	@echo 'Finished building target: $@'
	@echo ' '

# Other Targets
clean:
	-$(RM) $(OBJS)$(C_DEPS)$(EXECUTABLES) inputTypeOption
	-@echo ' '

.PHONY: all clean dependents
.SECONDARY:

-include ../makefile.targets
