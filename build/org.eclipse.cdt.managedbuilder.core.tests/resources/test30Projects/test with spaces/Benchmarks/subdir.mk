################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../main\ with\ spaces.c 

OBJS += \
./main\ with\ spaces.o 

C_DEPS += \
./main\ with\ spaces.d 


# Each subdirectory must supply rules for building sources it contributes
main\ with\ spaces.o: ../main\ with\ spaces.c subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"main with spaces.d" -MT"$@" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


clean: clean--2e-

clean--2e-:
	-$(RM) ./main\ with\ spaces.d ./main\ with\ spaces.o

.PHONY: clean--2e-

