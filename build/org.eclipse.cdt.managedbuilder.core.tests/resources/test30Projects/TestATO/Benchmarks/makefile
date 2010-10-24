################################################################################
# Automatically-generated file. Do not edit!
################################################################################

-include ../makefile.init

RM := rm -rf

# All of the sources participating in the build are defined here
-include sources.mk
-include subdir.mk
-include objects.mk

-include ../makefile.defs

# Add inputs and outputs from these tool invocations to the build variables 

# All Target
all: TestATO.out

# Tool invocations
TestATO.out: $(OBJS) $(OPT_SRCS)
	@echo 'Building target: $@'
	@echo 'Invoking: AssignToOption Executable Linker'
	ATOL -opt../TestATO1.opt -opt../TestATO2.opt -o "TestATO.out" $(OBJS)
	@echo 'Finished building target: $@'
	@echo ' '

# Other Targets
clean:
	-$(RM) $(ASSIGNTOOPTION_EXECUTABLE_OUTPUTS)$(OBJS) TestATO.out
	-@echo ' '

.PHONY: all clean dependents
.SECONDARY:

-include ../makefile.targets
