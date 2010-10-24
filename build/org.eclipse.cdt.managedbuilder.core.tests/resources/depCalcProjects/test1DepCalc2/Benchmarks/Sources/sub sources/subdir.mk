################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../Sources/sub\ sources/func\ 3.c 

OBJS += \
./Sources/sub\ sources/func\ 3.o 

C_DEPS += \
./Sources/sub\ sources/func\ 3.d 


# Each subdirectory must supply rules for building sources it contributes
Sources/sub\ sources/func\ 3.o: ../Sources/sub\ sources/func\ 3.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -I../Headers -I../Sources/sub\ sources -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"Sources/sub sources/func 3.d" -MT"Sources/sub\ sources/func\ 3.d" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


