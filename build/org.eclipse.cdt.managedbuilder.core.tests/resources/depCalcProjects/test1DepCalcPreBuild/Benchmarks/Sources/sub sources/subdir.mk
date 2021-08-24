################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../Sources/sub\ sources/func\ 3.c 

C_DEPS += \
./Sources/sub\ sources/func\ 3.d 

OBJS += \
./Sources/sub\ sources/func\ 3.o 


# Each subdirectory must supply rules for building sources it contributes
Sources/sub\ sources/func\ 3.o: ../Sources/sub\ sources/func\ 3.c Sources/sub\ sources/subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -I../Headers -I../Sources/sub\ sources -O0 -g3 -Wall -c -fmessage-length=0 -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '

Sources/sub\ sources/func\ 3.d: ../Sources/sub\ sources/func\ 3.c Sources/sub\ sources/subdir.mk
	@echo 'Regenerating dependency file: $@'
	gcc -w -MM -MP -MT"Sources/sub\ sources/func\ 3.d" -MT"Sources/sub\ sources/func\ 3.o" -I../Headers -I../Sources/sub\ sources -MF "$@" "$<"
	@echo ' '


clean: clean-Sources-2f-sub-20-sources

clean-Sources-2f-sub-20-sources:
	-$(RM) ./Sources/sub\ sources/func\ 3.d ./Sources/sub\ sources/func\ 3.o

.PHONY: clean-Sources-2f-sub-20-sources

