################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../Sources/func1.c \
../Sources/func2.c \
../Sources/func4.c 

OBJS += \
./Sources/func1.o \
./Sources/func2.o \
./Sources/func4.o 

C_DEPS += \
./Sources/func1.d \
./Sources/func2.d \
./Sources/func4.d 


# Each subdirectory must supply rules for building sources it contributes
Sources/%.o: ../Sources/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -I../Headers -I../Sources/sub\ sources -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '

Sources/func2.o: ../Sources/func2.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -I../Headers -I../Sources/sub\ sources -O3 -g3 -Wall -c -MMD -MP -MF"$(@:%.o=%.d)" -MT"Sources/func2.d" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


