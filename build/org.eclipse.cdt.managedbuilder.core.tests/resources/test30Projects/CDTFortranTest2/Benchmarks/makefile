################################################################################
# Automatically-generated file. Do not edit!
################################################################################

-include ../makefile.init

RM := rm -rf

# All of the sources participating in the build are defined here
-include sources.mk
-include module/subdir.mk
-include Sources/subdir.mk
-include subdir.mk
-include objects.mk

-include ../makefile.defs

# Add inputs and outputs from these tool invocations to the build variables 

# All Target
all: CDTFortranTest2

# Tool invocations
CDTFortranTest2: $(OBJS) $(USER_OBJS)
	@echo 'Building target: $@'
	@echo 'Invoking: Linker'
	mylink  -o "CDTFortranTest2" $(OBJS) $(USER_OBJS) $(LIBS)
	@echo 'Finished building target: $@'
	@echo ' '

# Other Targets
clean:
	-$(RM) $(OBJS)$(MODS)$(EXECUTABLES) CDTFortranTest2
	-@echo ' '

.PHONY: all clean dependents

-include ../makefile.targets
