################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../rcbsBasicTest.c 

OBJS += \
./rcbsBasicTest.o 


# Each subdirectory must supply rules for building sources it contributes
rcbsBasicTest.o: ../rcbsBasicTest.c subdir.mk
	@echo 'Building file: $<'
	@echo 'Now executing custom build step for rcbsBasicTest debug config'
	gcc -g -c ../rcbsBasicTest.c -o ./rcbsBasicTest.o
	@echo 'Finished building: $<'
	@echo ' '


clean: clean--2e-

clean--2e-:
	-$(RM) ./rcbsBasicTest.o

.PHONY: clean--2e-

