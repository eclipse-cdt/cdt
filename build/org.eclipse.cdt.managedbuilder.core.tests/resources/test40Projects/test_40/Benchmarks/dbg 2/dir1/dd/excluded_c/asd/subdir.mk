################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../dir1/dd/excluded_c/asd/s.cpp 

OBJS += \
./dir1/dd/excluded_c/asd/s.o 

CPP_DEPS += \
./dir1/dd/excluded_c/asd/s.d 


# Each subdirectory must supply rules for building sources it contributes
dir1/dd/excluded_c/asd/%.o: ../dir1/dd/excluded_c/asd/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: Test 4.0 ToolName.compiler.gnu.cpp'
	g++ -O2 -g -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


