################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../Sources/func1.c \
../Sources/func2.c \
../Sources/func4.c 

C_DEPS += \
./Sources/func1.d \
./Sources/func2.d \
./Sources/func4.d 

OBJS += \
./Sources/func1.o \
./Sources/func2.o \
./Sources/func4.o 


# Each subdirectory must supply rules for building sources it contributes
Sources/%.o: ../Sources/%.c Sources/subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -I../Headers -I../Sources/sub\ sources -O0 -g3 -Wall -c -fmessage-length=0 -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '

Sources/%.d: ../Sources/%.c Sources/subdir.mk
	@echo 'Regenerating dependency file: $@'
	gcc -w -MM -MP -MT"$@" -MT"$(@:%.d=%.o)" -I../Headers -I../Sources/sub\ sources -MF "$@" "$<"
	@echo ' '


clean: clean-Sources

clean-Sources:
	-$(RM) ./Sources/func1.d ./Sources/func1.o ./Sources/func2.d ./Sources/func2.o ./Sources/func4.d ./Sources/func4.o

.PHONY: clean-Sources

