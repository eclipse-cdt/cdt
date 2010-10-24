################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../dir1/dd/ff/vbn.c 

CPP_SRCS += \
../dir1/dd/ff/zxc.cpp 

OBJS += \
./dir1/dd/ff/vbn.o \
./dir1/dd/ff/zxc.o 

C_DEPS += \
./dir1/dd/ff/vbn.d 

CPP_DEPS += \
./dir1/dd/ff/zxc.d 


# Each subdirectory must supply rules for building sources it contributes
dir1/dd/ff/%.o: ../dir1/dd/ff/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: Test 4.0 ToolName.compiler.gnu.c'
	gcc -O2 -g -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '

dir1/dd/ff/%.o: ../dir1/dd/ff/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: Test 4.0 ToolName.compiler.gnu.cpp'
	g++ -O2 -g -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


