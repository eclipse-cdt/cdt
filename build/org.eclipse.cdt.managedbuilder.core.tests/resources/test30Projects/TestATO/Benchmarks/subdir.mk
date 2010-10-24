################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../TestATO.c 

OPT_SRCS += \
../TestATO1.opt \
../TestATO2.opt 

COP_SRCS += \
../TestATO.cop 

OBJS += \
./TestATO.obj 


# Each subdirectory must supply rules for building sources it contributes
%.obj: ../%.c
	@echo 'Building file: $<'
	@echo 'Invoking: AssignToOption Compiler'
	ATOC -opt../TestATO.cop  "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


