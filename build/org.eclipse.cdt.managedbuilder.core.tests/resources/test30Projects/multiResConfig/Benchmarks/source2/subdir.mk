################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../source2/Class2.cpp 

OBJS += \
./source2/Class2.o 

CPP_DEPS += \
./source2/Class2.d 


# Each subdirectory must supply rules for building sources it contributes
source2/%.o: ../source2/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: MBS30.compiler.gnu.cpp'
	g++ -I../headers -O0 -g3 -Wall -c -fmessage-length=0 -o "$@" "$<" && \
	echo -n '$(@:%.o=%.d)' $(dir $@) > '$(@:%.o=%.d)' && \
	g++ -MM -MG -P -w -I../headers -O0 -g3 -Wall -c -fmessage-length=0   "$<" >> '$(@:%.o=%.d)'
	@echo 'Finished building: $<'
	@echo ' '


