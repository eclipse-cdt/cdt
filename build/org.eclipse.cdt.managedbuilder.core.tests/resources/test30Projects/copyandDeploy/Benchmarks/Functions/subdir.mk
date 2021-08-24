################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
X_SRCS += \
../Functions/Func1.x 

CS += \
./Functions/Func1.c 

C_DEPS += \
./Functions/Func1.d 

OBJS += \
./Functions/Func1.o 


# Each subdirectory must supply rules for building sources it contributes
Functions/%.c: ../Functions/%.x Functions/subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: Copy tool'
	cp "$<" "$@"
	@echo 'Finished building: $<'
	@echo ' '

Functions/%.o: ./Functions/%.c Functions/subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: MBS30.compiler.gnu.c'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -v -o "$@" "$<" && \
	echo -n '$(@:%.o=%.d)' $(dir $@) > '$(@:%.o=%.d)' && \
	gcc -MM -MG -P -w -O0 -g3 -Wall -c -fmessage-length=0 -v   "$<" >> '$(@:%.o=%.d)'
	@echo 'Finished building: $<'
	@echo ' '


clean: clean-Functions

clean-Functions:
	-$(RM) ./Functions/Func1.c ./Functions/Func1.d ./Functions/Func1.o

.PHONY: clean-Functions

