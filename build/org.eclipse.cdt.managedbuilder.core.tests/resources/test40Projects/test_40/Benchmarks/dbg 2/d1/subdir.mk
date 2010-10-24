################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../d1/q.cpp \
../d1/u.cpp \
../d1/w.cpp 

OBJS += \
./d1/q.o \
./d1/u.o \
./d1/w.o 

CPP_DEPS += \
./d1/q.d \
./d1/u.d \
./d1/w.d 


# Each subdirectory must supply rules for building sources it contributes
d1/%.o: ../d1/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: Test 4.0 ToolName.compiler.gnu.cpp'
	g++ -Id1_rel/path -I../d1_proj/rel/path -I/d1_abs/path -Ic:/d1_abs/path -I"D:\d1_docs\incs" -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


