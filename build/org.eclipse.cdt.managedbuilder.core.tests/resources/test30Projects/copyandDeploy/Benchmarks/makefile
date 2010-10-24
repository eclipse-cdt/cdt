################################################################################
# Automatically-generated file. Do not edit!
################################################################################

-include ../makefile.init

RM := rm -rf

# All of the sources participating in the build are defined here
-include sources.mk
-include subdir.mk
-include Functions/subdir.mk
-include objects.mk

ifneq ($(MAKECMDGOALS),clean)
ifneq ($(strip $(C_DEPS)),)
-include $(C_DEPS)
endif
endif

-include ../makefile.defs

# Add inputs and outputs from these tool invocations to the build variables 
EXECUTABLES += \
USER_OBJS.foo \


# All Target
all: TestCopyandDeploy

# Tool invocations
TestCopyandDeploy: $(EXECUTABLES)
	@echo 'Building target: $@'
	@echo 'Invoking: Deploy tool'
	cp $(EXECUTABLES) "TestCopyandDeploy"
	@echo 'Finished building target: $@'
	@echo ' '

USER_OBJS.foo: $(OBJS) $(USER_OBJS)
	@echo 'Invoking: MBS30.linker.gnu.c'
	gcc  -o "USER_OBJS.foo" $(OBJS) $(USER_OBJS) $(LIBS)
	@echo 'Finished building: $@'
	@echo ' '

# Other Targets
clean:
	-$(RM) $(OBJS)$(FINAL)$(C_DEPS)$(CS)$(EXECUTABLES) TestCopyandDeploy
	-@echo ' '

.PHONY: all clean dependents
.SECONDARY:

-include ../makefile.targets
