################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../d1/d2/d3/t.cpp \
../d1/d2/d3/y.cpp 

CPP_DEPS += \
./d1/d2/d3/t.d \
./d1/d2/d3/y.d 

OBJS += \
./d1/d2/d3/t.o \
./d1/d2/d3/y.o 


# Each subdirectory must supply rules for building sources it contributes
d1/d2/d3/%.o: ../d1/d2/d3/%.cpp d1/d2/d3/subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: Test 4.0 ToolName.compiler.gnu.cpp'
	g++ -Id3_rel/path -I/d3_abs/path -Ic:/d3_abs/path -I"D:\d3_docs\incs" -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$@" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


clean: clean-d1-2f-d2-2f-d3

clean-d1-2f-d2-2f-d3:
	-$(RM) ./d1/d2/d3/t.d ./d1/d2/d3/t.o ./d1/d2/d3/y.d ./d1/d2/d3/y.o

.PHONY: clean-d1-2f-d2-2f-d3

