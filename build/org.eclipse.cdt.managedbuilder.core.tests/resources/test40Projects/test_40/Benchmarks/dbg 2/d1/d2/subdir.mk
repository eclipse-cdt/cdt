################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../d1/d2/e.cpp \
../d1/d2/r.cpp 

CPP_DEPS += \
./d1/d2/e.d \
./d1/d2/r.d 

OBJS += \
./d1/d2/e.o \
./d1/d2/r.o 


# Each subdirectory must supply rules for building sources it contributes
d1/d2/e.o: ../d1/d2/e.cpp d1/d2/subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: Test 4.0 ToolName.compiler.gnu.cpp'
	g++ -Id2_ecpp_rel/path -I../d2_ecpp_proj/rel/path -I/d2_ecpp_abs/path -Ic:/d2_ecpp_abs/path -Id2_rel/path -I../d2_proj/rel/path -I/d2_abs/path -Ic:/d2_abs/path -Id1_rel/path -I../d1_proj/rel/path -I/d1_abs/path -Ic:/d1_abs/path -I"D:\d1_docs\incs" -I"D:\d2_docs\incs" -I"D:\d2_ecpp_docs\incs" -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$@" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '

d1/d2/%.o: ../d1/d2/%.cpp d1/d2/subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: Test 4.0 ToolName.compiler.gnu.cpp'
	g++ -Id2_rel/path -I../d2_proj/rel/path -I/d2_abs/path -Ic:/d2_abs/path -Id1_rel/path -I../d1_proj/rel/path -I/d1_abs/path -Ic:/d1_abs/path -I"D:\d1_docs\incs" -I"D:\d2_docs\incs" -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$@" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


clean: clean-d1-2f-d2

clean-d1-2f-d2:
	-$(RM) ./d1/d2/e.d ./d1/d2/e.o ./d1/d2/r.d ./d1/d2/r.o

.PHONY: clean-d1-2f-d2

