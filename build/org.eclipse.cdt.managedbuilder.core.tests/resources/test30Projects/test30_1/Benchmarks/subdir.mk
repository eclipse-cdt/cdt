################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
TAR_SRCS += \
../test.tar 

C_DEPS += \
./f1.d \
./f2.d 

OBJS += \
./f1.oprestripped \
./f2.oprestripped 

STRIPPEDOBJS += \
./f1.o \
./f2.o 

TEST30_1_GNU_SO_CJPEG_OUTPUT_OUTPUTS += \
./CDT.jpeg 

TEST30_1_GNU_SO_TAR_OUTPUTC_OUTPUTS += \
./f1.c \
./f2.c \
./CDT.bmp 


# Each subdirectory must supply rules for building sources it contributes
f1.c: ../test.tar subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: Un-tar'
	tar -xf "$<"
	@echo 'Finished building: $<'
	@echo ' '

f2.c: f1.c
test_ar.h: f1.c
CDT.bmp: f1.c

%.oprestripped: ./%.c subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: MBS30.compiler.gnu.c'
	gcc -O2 -g -Wall -c -fmessage-length=0 -v -o "$@" "$<" && \
	echo -n '$(@:%.oprestripped=%.d)' $(dir $@) > '$(@:%.oprestripped=%.d)' && \
	gcc -MM -MG -P -w -O2 -g -Wall -c -fmessage-length=0 -v   "$<" >> '$(@:%.oprestripped=%.d)'
	@echo 'Finished building: $<'
	@echo ' '

%.o: ./%.oprestripped subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: Strip object file'
	strip --preserve-dates -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '

%.jpeg: ./%.bmp subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: Convert to jpeg'
	cjpeg  -outfile "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


clean: clean--2e-

clean--2e-:
	-$(RM) ./CDT.bmp ./CDT.jpeg ./f1.c ./f1.d ./f1.o ./f1.oprestripped ./f2.c ./f2.d ./f2.o ./f2.oprestripped ./test_ar.h

.PHONY: clean--2e-

