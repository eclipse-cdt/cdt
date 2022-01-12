################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
COP_SRCS += \
../TestATO.cop 

C_SRCS += \
../TestATO.c 

OPT_SRCS += \
../TestATO1.opt \
../TestATO2.opt 

OBJS += \
./TestATO.obj 


# Each subdirectory must supply rules for building sources it contributes
%.obj: ../%.c subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: AssignToOption Compiler'
	ATOC -opt../TestATO.cop  "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


clean: clean--2e-

clean--2e-:
	-$(RM) ./TestATO.obj

.PHONY: clean--2e-

