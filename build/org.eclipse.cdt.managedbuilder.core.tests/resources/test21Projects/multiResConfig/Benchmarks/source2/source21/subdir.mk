################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../source2/source21/Class21.cpp 

OBJS += \
./source2/source21/Class21.o 

CPP_DEPS += \
./source2/source21/Class21.d 


# Each subdirectory must supply rules for building sources it contributes
source2/source21/Class21.o: ../source2/source21/Class21.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: compiler.gnu.cpp'
	g++ -DRESSPEC -I../headers -O0 -g3 -Wall -c -o "$@" "$<" && \
	echo -n '$(@:%.o=%.d)' $(dir $@) > '$(@:%.o=%.d)' && \
	g++ -MM -MG -P -w -DRESSPEC -I../headers -O0 -g3 -Wall -c   "$<" >> '$(@:%.o=%.d)'
	@echo 'Finished building: $<'
	@echo ' '


