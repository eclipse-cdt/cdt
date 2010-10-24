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
	gcc -I../Headers -I../Sources/sub\ sources -O0 -g3 -Wall -c -fmessage-length=0 -o "$@" "$<" && \
	echo -n '$(@:%.o=%.d)' $(dir $@) > '$(@:%.o=%.d)' && \
	gcc -MM -MG -P -w -I../Headers -I../Sources/sub\ sources -O0 -g3 -Wall -c -fmessage-length=0   "$<" >> '$(@:%.o=%.d)'
	@echo 'Finished building: $<'
	@echo ' '


