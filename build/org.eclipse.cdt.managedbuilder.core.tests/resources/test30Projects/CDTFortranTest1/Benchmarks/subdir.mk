################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
F90_UPPER_SRCS += \
../AVERAGE.F90 \
../MAIN.F90 \
../MODULE.F90 

OBJS += \
./AVERAGE.obj \
./MAIN.obj \
./MODULE.obj 


# Each subdirectory must supply rules for building sources it contributes
%.obj: ../%.F90
	@echo 'Building file: $<'
	@echo 'Invoking: Test Fortran Compiler'
	myfort  -c -object:"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '

MAIN.obj: AVE_CALCULATOR.mod ../MODULE.F90

./AVE_CALCULATOR.mod: MODULE.obj


