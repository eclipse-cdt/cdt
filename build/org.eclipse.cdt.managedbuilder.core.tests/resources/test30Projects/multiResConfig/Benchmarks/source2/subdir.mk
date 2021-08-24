################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../source2/Class2.cpp 

CPP_DEPS += \
./source2/Class2.d 

OBJS += \
./source2/Class2.o 


# Each subdirectory must supply rules for building sources it contributes
source2/%.o: ../source2/%.cpp source2/subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: MBS30.compiler.gnu.cpp'
	g++ -I../headers -O0 -g3 -Wall -c -fmessage-length=0 -o "$@" "$<" && \
	echo -n '$(@:%.o=%.d)' $(dir $@) > '$(@:%.o=%.d)' && \
	g++ -MM -MG -P -w -I../headers -O0 -g3 -Wall -c -fmessage-length=0   "$<" >> '$(@:%.o=%.d)'
	@echo 'Finished building: $<'
	@echo ' '


clean: clean-source2

clean-source2:
	-$(RM) ./source2/Class2.d ./source2/Class2.o

.PHONY: clean-source2

