################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
C:/Documents\ and\ Settings/ltreggia/CDTMBSTest/f1.c \
C:/Documents\ and\ Settings/ltreggia/CDTMBSTest/f2.c 

OBJS += \
./f1.o \
./f2.o 

C_DEPS += \
./f1.d \
./f2.d 


# Each subdirectory must supply rules for building sources it contributes
f1.o: C:/Documents\ and\ Settings/agvozdev/CDTMBSTest/f1.c
	@echo 'Building file: $<'
	@echo 'Invoking: compiler.gnu.c'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -o "$@" "$<" && \
	echo -n '$(@:%.o=%.d)' $(dir $@) > '$(@:%.o=%.d)' && \
	gcc -MM -MG -P -w -O0 -g3 -Wall -c -fmessage-length=0  "$<" >> '$(@:%.o=%.d)'
	@echo 'Finished building: $<'
	@echo ' '

f2.o: C:/Documents\ and\ Settings/agvozdev/CDTMBSTest/f2.c
	@echo 'Building file: $<'
	@echo 'Invoking: compiler.gnu.c'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -o "$@" "$<" && \
	echo -n '$(@:%.o=%.d)' $(dir $@) > '$(@:%.o=%.d)' && \
	gcc -MM -MG -P -w -O0 -g3 -Wall -c -fmessage-length=0  "$<" >> '$(@:%.o=%.d)'
	@echo 'Finished building: $<'
	@echo ' '


