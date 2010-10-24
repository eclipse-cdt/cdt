################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../main.c 

OBJS += \
./main.o 

C_DEPS += \
./main.d 


# Each subdirectory must supply rules for building sources it contributes
main.o: ../main.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -I../Headers -I../Sources/sub\ sources -O0 -g3 -c -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '

main.d: ../main.c
	@echo 'Regenerating dependency file: $@'
	gcc -w -MM -MP -MT"main.d" -MT"main.o" -I../Headers -I../Sources/sub\ sources -MF "$@" "$<"
	@echo ' '


