################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../main.c 

C_DEPS += \
./main.d 

OBJS += \
./main.o 


# Each subdirectory must supply rules for building sources it contributes
main.o: ../main.c subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -I../Headers -I../Sources/sub\ sources -O0 -g3 -c -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '

main.d: ../main.c subdir.mk
	@echo 'Regenerating dependency file: $@'
	gcc -w -MM -MP -MT"main.d" -MT"main.o" -I../Headers -I../Sources/sub\ sources -MF "$@" "$<"
	@echo ' '


clean: clean--2e-

clean--2e-:
	-$(RM) ./main.d ./main.o

.PHONY: clean--2e-

