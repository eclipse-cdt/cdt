################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../d1_1/i.cpp \
../d1_1/o.cpp 

OBJS += \
./d1_1/i.o \
./d1_1/o.o 

CPP_DEPS += \
./d1_1/i.d \
./d1_1/o.d 


# Each subdirectory must supply rules for building sources it contributes
d1_1/%.o: ../d1_1/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: Test 4.0 ToolName.compiler.gnu.cpp'
	g++ -Id1_1_rel/path -I/d1_1_abs/path -Ic:/d1_1_abs/path -I"D:\d1_1_docs\incs" -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


