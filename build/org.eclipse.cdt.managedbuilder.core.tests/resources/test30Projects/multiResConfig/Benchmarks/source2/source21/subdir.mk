################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../source2/source21/Class21.cpp 

CPP_DEPS += \
./source2/source21/Class21.d 

OBJS += \
./source2/source21/Class21.o 


# Each subdirectory must supply rules for building sources it contributes
source2/source21/Class21.o: ../source2/source21/Class21.cpp source2/source21/subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: MBS30.compiler.gnu.cpp'
	g++ -DRESCFG -I../headers -O0 -g3 -Wall -c -fmessage-length=0 -o "$@" "$<" && \
	echo -n '$(@:%.o=%.d)' $(dir $@) > '$(@:%.o=%.d)' && \
	g++ -MM -MG -P -w -DRESCFG -I../headers -O0 -g3 -Wall -c -fmessage-length=0   "$<" >> '$(@:%.o=%.d)'
	@echo 'Finished building: $<'
	@echo ' '


clean: clean-source2-2f-source21

clean-source2-2f-source21:
	-$(RM) ./source2/source21/Class21.d ./source2/source21/Class21.o

.PHONY: clean-source2-2f-source21

