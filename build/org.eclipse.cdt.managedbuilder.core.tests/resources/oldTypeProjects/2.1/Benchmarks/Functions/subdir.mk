################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../Functions/Func1.c 

C_DEPS += \
./Functions/Func1.d 

OBJS += \
./Functions/Func1.o 


# Each subdirectory must supply rules for building sources it contributes
Functions/%.o: ../Functions/%.c Functions/subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$@" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


clean: clean-Functions

clean-Functions:
	-$(RM) ./Functions/Func1.d ./Functions/Func1.o

.PHONY: clean-Functions

