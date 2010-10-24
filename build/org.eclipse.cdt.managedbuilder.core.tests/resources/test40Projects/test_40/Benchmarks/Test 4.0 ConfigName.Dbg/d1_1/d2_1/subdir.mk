################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../d1_1/d2_1/a.cpp \
../d1_1/d2_1/p.cpp 

OBJS += \
./d1_1/d2_1/a.o \
./d1_1/d2_1/p.o 

CPP_DEPS += \
./d1_1/d2_1/a.d \
./d1_1/d2_1/p.d 


# Each subdirectory must supply rules for building sources it contributes
d1_1/d2_1/%.o: ../d1_1/d2_1/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: Test 4.0 ToolName.compiler.gnu.cpp'
	g++ -Id2_1_rel/path -I/d2_1_abs/path -Ic:/d2_1_abs/path -Id1_1_rel/path -I/d1_1_abs/path -Ic:/d1_1_abs/path -Irel/path -I../proj/rel/path -I/abs/path -Ic:/abs/path -I"${WorkspaceDirPath}/test_40/dir1/dir2/dir3" -I"${WorkspaceDirPath}/test_40" -I"D:\docs\incs" -I"D:\d1_1_docs\incs" -I"D:\d2_1_docs\incs" -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


