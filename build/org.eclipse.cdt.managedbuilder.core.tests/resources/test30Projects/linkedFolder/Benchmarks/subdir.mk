################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
$(ROOT)/f1.c \
$(ROOT)/f2.c 

OBJS += \
${addprefix ./, \
f1.o \
f2.o \
}

DEPS += \
${addprefix ./, \
f1.d \
f2.d \
}


# Each subdirectory must supply rules for building sources it contributes
%.o: $(ROOT)/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: MBS30.compiler.gnu.c'
	@echo gcc -O0 -g3 -Wall -c -fmessage-length=0 -o$@ $<
	@gcc -O0 -g3 -Wall -c -fmessage-length=0 -o$@ $< && \
	echo -n $(@:%.o=%.d) $(dir $@) > $(@:%.o=%.d) && \
	gcc -MM -MG -P -w -O0 -g3 -Wall -c -fmessage-length=0  $< >> $(@:%.o=%.d)
	@echo 'Finished building: $<'
	@echo ' '


