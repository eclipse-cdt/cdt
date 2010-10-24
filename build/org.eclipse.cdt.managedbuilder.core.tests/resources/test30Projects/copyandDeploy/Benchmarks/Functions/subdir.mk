################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
X_SRCS += \
../Functions/Func1.x 

OBJS += \
./Functions/Func1.o 

C_DEPS += \
./Functions/Func1.d 

CS += \
./Functions/Func1.c 


# Each subdirectory must supply rules for building sources it contributes
Functions/%.c: ../Functions/%.x
	@echo 'Building file: $<'
	@echo 'Invoking: Copy tool'
	cp "$<" "$@"
	@echo 'Finished building: $<'
	@echo ' '

Functions/%.o: ./Functions/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: MBS30.compiler.gnu.c'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -v -o "$@" "$<" && \
	echo -n '$(@:%.o=%.d)' $(dir $@) > '$(@:%.o=%.d)' && \
	gcc -MM -MG -P -w -O0 -g3 -Wall -c -fmessage-length=0 -v   "$<" >> '$(@:%.o=%.d)'
	@echo 'Finished building: $<'
	@echo ' '


