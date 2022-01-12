################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../dir1/dd/excluded_c/asd/s.cpp 

CPP_DEPS += \
./dir1/dd/excluded_c/asd/s.d 

OBJS += \
./dir1/dd/excluded_c/asd/s.o 


# Each subdirectory must supply rules for building sources it contributes
dir1/dd/excluded_c/asd/%.o: ../dir1/dd/excluded_c/asd/%.cpp dir1/dd/excluded_c/asd/subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: Test 4.0 ToolName.compiler.gnu.cpp'
	g++ -O2 -g -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$@" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


clean: clean-dir1-2f-dd-2f-excluded_c-2f-asd

clean-dir1-2f-dd-2f-excluded_c-2f-asd:
	-$(RM) ./dir1/dd/excluded_c/asd/s.d ./dir1/dd/excluded_c/asd/s.o

.PHONY: clean-dir1-2f-dd-2f-excluded_c-2f-asd

