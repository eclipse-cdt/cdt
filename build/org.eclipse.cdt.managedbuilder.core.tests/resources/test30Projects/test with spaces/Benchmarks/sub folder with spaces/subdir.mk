################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../sub\ folder\ with\ spaces/foo\ with\ spaces.c 

OBJS += \
./sub\ folder\ with\ spaces/foo\ with\ spaces.o 

C_DEPS += \
./sub\ folder\ with\ spaces/foo\ with\ spaces.d 


# Each subdirectory must supply rules for building sources it contributes
sub\ folder\ with\ spaces/foo\ with\ spaces.o: ../sub\ folder\ with\ spaces/foo\ with\ spaces.c sub\ folder\ with\ spaces/subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"sub folder with spaces/foo with spaces.d" -MT"$@" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


clean: clean-sub-20-folder-20-with-20-spaces

clean-sub-20-folder-20-with-20-spaces:
	-$(RM) ./sub\ folder\ with\ spaces/foo\ with\ spaces.d ./sub\ folder\ with\ spaces/foo\ with\ spaces.o

.PHONY: clean-sub-20-folder-20-with-20-spaces

