################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../dir1/dd/ff/zxc.cpp 

C_SRCS += \
../dir1/dd/ff/vbn.c 

CPP_DEPS += \
./dir1/dd/ff/zxc.d 

C_DEPS += \
./dir1/dd/ff/vbn.d 

OBJS += \
./dir1/dd/ff/vbn.o \
./dir1/dd/ff/zxc.o 


# Each subdirectory must supply rules for building sources it contributes
dir1/dd/ff/%.o: ../dir1/dd/ff/%.c dir1/dd/ff/subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: Test 4.0 ToolName.compiler.gnu.c'
	gcc -O2 -g -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$@" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '

dir1/dd/ff/%.o: ../dir1/dd/ff/%.cpp dir1/dd/ff/subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: Test 4.0 ToolName.compiler.gnu.cpp'
	g++ -O2 -g -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$@" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


clean: clean-dir1-2f-dd-2f-ff

clean-dir1-2f-dd-2f-ff:
	-$(RM) ./dir1/dd/ff/vbn.d ./dir1/dd/ff/vbn.o ./dir1/dd/ff/zxc.d ./dir1/dd/ff/zxc.o

.PHONY: clean-dir1-2f-dd-2f-ff

