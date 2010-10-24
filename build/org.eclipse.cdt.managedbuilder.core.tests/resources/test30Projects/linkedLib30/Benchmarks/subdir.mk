################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
C:/Documents\ and\ Settings/ltreggia/CDTMBSTest/f1.c \
C:/Documents\ and\ Settings/ltreggia/CDTMBSTest/f1_30.c \
C:/Documents\ and\ Settings/ltreggia/CDTMBSTest/f2.c \
C:/Documents\ and\ Settings/ltreggia/CDTMBSTest/f2_30.c 

OBJS += \
./f1.o \
./f1_30.o \
./f2.o \
./f2_30.o 

C_DEPS += \
./f1.d \
./f1_30.d \
./f2.d \
./f2_30.d 


# Each subdirectory must supply rules for building sources it contributes
f1.o: C:/Documents\ and\ Settings/ltreggia/CDTMBSTest/f1.c
	@echo 'Building file: $<'
	@echo 'Invoking: MBS30.compiler.gnu.c'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -v -o "$@" "$<" && \
	echo -n '$(@:%.o=%.d)' $(dir $@) > '$(@:%.o=%.d)' && \
	gcc -MM -MG -P -w -O0 -g3 -Wall -c -fmessage-length=0 -v  "$<" >> '$(@:%.o=%.d)'
	@echo 'Finished building: $<'
	@echo ' '

f1_30.o: C:/Documents\ and\ Settings/agvozdev/CDTMBSTest/f1_30.c
	@echo 'Building file: $<'
	@echo 'Invoking: MBS30.compiler.gnu.c'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -v -o "$@" "$<" && \
	echo -n '$(@:%.o=%.d)' $(dir $@) > '$(@:%.o=%.d)' && \
	gcc -MM -MG -P -w -O0 -g3 -Wall -c -fmessage-length=0 -v  "$<" >> '$(@:%.o=%.d)'
	@echo 'Finished building: $<'
	@echo ' '

f2.o: C:/Documents\ and\ Settings/ltreggia/CDTMBSTest/f2.c
	@echo 'Building file: $<'
	@echo 'Invoking: MBS30.compiler.gnu.c'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -v -o "$@" "$<" && \
	echo -n '$(@:%.o=%.d)' $(dir $@) > '$(@:%.o=%.d)' && \
	gcc -MM -MG -P -w -O0 -g3 -Wall -c -fmessage-length=0 -v  "$<" >> '$(@:%.o=%.d)'
	@echo 'Finished building: $<'
	@echo ' '

f2_30.o: C:/Documents\ and\ Settings/agvozdev/CDTMBSTest/f2_30.c
	@echo 'Building file: $<'
	@echo 'Invoking: MBS30.compiler.gnu.c'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -v -o "$@" "$<" && \
	echo -n '$(@:%.o=%.d)' $(dir $@) > '$(@:%.o=%.d)' && \
	gcc -MM -MG -P -w -O0 -g3 -Wall -c -fmessage-length=0 -v  "$<" >> '$(@:%.o=%.d)'
	@echo 'Finished building: $<'
	@echo ' '


