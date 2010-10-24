################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
X_SRCS += \
../main.x 

OBJS += \
./main.o 

C_DEPS += \
./main.d 

CS += \
./main.c 


# Each subdirectory must supply rules for building sources it contributes
%.c: ../%.x
	@echo 'Building file: $<'
	@echo 'Invoking: Copy tool'
	cp "$<" "$@"
	@echo 'Finished building: $<'
	@echo ' '

%.o: ./%.c
	@echo 'Building file: $<'
	@echo 'Invoking: MBS30.compiler.gnu.c'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -v -o "$@" "$<" && \
	echo -n '$(@:%.o=%.d)' $(dir $@) > '$(@:%.o=%.d)' && \
	gcc -MM -MG -P -w -O0 -g3 -Wall -c -fmessage-length=0 -v   "$<" >> '$(@:%.o=%.d)'
	@echo 'Finished building: $<'
	@echo ' '


