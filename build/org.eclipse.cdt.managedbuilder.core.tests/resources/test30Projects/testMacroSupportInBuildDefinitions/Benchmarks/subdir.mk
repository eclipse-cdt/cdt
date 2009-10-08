################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
FOO_SRCS += \
../somefile.foo 

BAR_FILES += \
./this_is_a_test_prefix_with_a_macro_for_the_project_name_between_here_testMacroSupportInBuildDefinitions_and_heresomefile.bar 


# Each subdirectory must supply rules for building sources it contributes
this_is_a_test_prefix_with_a_macro_for_the_project_name_between_here_testMacroSupportInBuildDefinitions_and_here%.bar: ../%.foo
	@echo 'Building file: $<'
	@echo 'Invoking: Foo Tool'
	cp "$<" "$@"
	@echo 'Finished building: $<'
	@echo ' '


