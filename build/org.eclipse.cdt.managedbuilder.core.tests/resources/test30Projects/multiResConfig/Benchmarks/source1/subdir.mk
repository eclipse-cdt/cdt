################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../source1/Class1.cpp 

CPP_DEPS += \
./source1/Class1.d 

OBJS += \
./source1/Class1.o 


# Each subdirectory must supply rules for building sources it contributes
source1/%.o: ../source1/%.cpp source1/subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: MBS30.compiler.gnu.cpp'
	g++ -I../headers -O0 -g3 -Wall -c -fmessage-length=0 -o "$@" "$<" && \
	echo -n '$(@:%.o=%.d)' $(dir $@) > '$(@:%.o=%.d)' && \
	g++ -MM -MG -P -w -I../headers -O0 -g3 -Wall -c -fmessage-length=0   "$<" >> '$(@:%.o=%.d)'
	@echo 'Finished building: $<'
	@echo ' '


clean: clean-source1

clean-source1:
	-$(RM) ./source1/Class1.d ./source1/Class1.o

.PHONY: clean-source1

