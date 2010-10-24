################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../picksUpGlobalAbsoluteInputOptionPath.c \
../resourceSettingAbsoluteCompiler.c \
../resourceSettingRelativeCompiler.c 

OBJS += \
./picksUpGlobalAbsoluteInputOptionPath.o \
./resourceSettingAbsoluteCompiler.o \
./resourceSettingRelativeCompiler.o 

C_DEPS += \
./picksUpGlobalAbsoluteInputOptionPath.d \
./resourceSettingAbsoluteCompiler.d \
./resourceSettingRelativeCompiler.d 


# Each subdirectory must supply rules for building sources it contributes
%.o: ../%.c C:/An\ Absolute\ Path\ With\ Spaces/foo.compiler C:/An\ Absolute\ Path\ With\ Spaces/foo.noquotes.compiler
	@echo 'Building file: $<'
	@echo 'Invoking: org.eclipse.cdt.managedbuilder.core.tests.inputTypeOptionMakefileRenderingTest.c.compiler'
	gcc -O3 -Wall -c -fmessage-length=0 -v --compilerInputTypeOption="C:\An Absolute Path With Spaces\foo.compiler" --compilerInputTypeOption=C:\An Absolute Path With Spaces\foo.noquotes.compiler -o "$@" "$<" && \
	echo -n '$(@:%.o=%.d)' $(dir $@) > '$(@:%.o=%.d)' && \
	gcc -MM -MG -P -w -O3 -Wall -c -fmessage-length=0 -v --compilerInputTypeOption="C:\An Absolute Path With Spaces\foo.compiler" --compilerInputTypeOption=C:\An Absolute Path With Spaces\foo.noquotes.compiler   "$<" >> '$(@:%.o=%.d)'
	@echo 'Finished building: $<'
	@echo ' '

resourceSettingAbsoluteCompiler.o: ../resourceSettingAbsoluteCompiler.c D:/An\ Absolute\ Path\ With\ Spaces/resource.foo.compiler D:/An\ Absolute\ Path\ With\ Spaces/resource.foo.noquotes.compiler
	@echo 'Building file: $<'
	@echo 'Invoking: org.eclipse.cdt.managedbuilder.core.tests.inputTypeOptionMakefileRenderingTest.c.compiler'
	gcc -O3 -Wall -c -fmessage-length=0 -v --compilerInputTypeOption="D:\An Absolute Path With Spaces\resource.foo.compiler" --compilerInputTypeOption=D:\An Absolute Path With Spaces\resource.foo.noquotes.compiler -o "$@" "$<" && \
	echo -n '$(@:%.o=%.d)' $(dir $@) > '$(@:%.o=%.d)' && \
	gcc -MM -MG -P -w -O3 -Wall -c -fmessage-length=0 -v --compilerInputTypeOption="D:\An Absolute Path With Spaces\resource.foo.compiler" --compilerInputTypeOption=D:\An Absolute Path With Spaces\resource.foo.noquotes.compiler   "$<" >> '$(@:%.o=%.d)'
	@echo 'Finished building: $<'
	@echo ' '

resourceSettingRelativeCompiler.o: ../resourceSettingRelativeCompiler.c ../A\ Folder\ With\ Spaces/foo.compiler ../A\ Folder\ With\ Spaces/foo.noquotes.compiler
	@echo 'Building file: $<'
	@echo 'Invoking: org.eclipse.cdt.managedbuilder.core.tests.inputTypeOptionMakefileRenderingTest.c.compiler'
	gcc -O3 -Wall -c -fmessage-length=0 -v --compilerInputTypeOption="A Folder With Spaces/foo.compiler" --compilerInputTypeOption=A Folder With Spaces/foo.noquotes.compiler -o "$@" "$<" && \
	echo -n '$(@:%.o=%.d)' $(dir $@) > '$(@:%.o=%.d)' && \
	gcc -MM -MG -P -w -O3 -Wall -c -fmessage-length=0 -v --compilerInputTypeOption="A Folder With Spaces/foo.compiler" --compilerInputTypeOption=A Folder With Spaces/foo.noquotes.compiler   "$<" >> '$(@:%.o=%.d)'
	@echo 'Finished building: $<'
	@echo ' '


