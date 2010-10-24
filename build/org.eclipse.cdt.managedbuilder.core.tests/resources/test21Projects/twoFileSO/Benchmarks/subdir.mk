################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../so1.c \
../so2.c 

OBJS += \
./so1.o \
./so2.o 

C_DEPS += \
./so1.d \
./so2.d 


# Each subdirectory must supply rules for building sources it contributes
%.o: ../%.c
	@echo 'Building file: $<'
	@echo 'Invoking: compiler.gnu.cpp'
	g++ -DXXX -O0 -g3 -Wall -c -o "$@" "$<" && \
	echo -n '$(@:%.o=%.d)' $(dir $@) > '$(@:%.o=%.d)' && \
	g++ -MM -MG -P -w -DXXX -O0 -g3 -Wall -c   "$<" >> '$(@:%.o=%.d)'
	@echo 'Finished building: $<'
	@echo ' '


