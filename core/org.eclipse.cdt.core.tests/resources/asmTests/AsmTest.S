/*
 * AsmTest.S
 */

#include <include1.h>
#include "include2.h"

	.globl globalLabel1
	.global globalLabel2, globalLabel3

nonGlobalLabel:
	nop
	ret   @ arm style comment
/* end */

globalLabel1:
	movl    %esp, %eax
	movl    %eax, storage
otherLabel1:
	movb	$0x81, %ah  # end-of-line comment
	jmp	globalLabel2

; data	
storage:
	.long 0
/* end */

#define MACRO mmm

	/*
	 * block comment
	 */	
globalLabel2:
	movl    %esp, %eax
	movl    %eax, storage

; line comment

otherLabel2:
	movb	$0x81, %ah  ; end-of-line comment
	jmp	globalLabel3    # comment
/* end */

globalLabel3:
	movl    %esp, %eax
	movl    %eax, storage
	
otherLabel3:
	movb	$0x81, %ah
	jmp	globalLabel3
	nop
/* end */

	.global alloca
alloca:
	popl %edx
	popl %eax
	addl $3,%eax
	andl $0xfffffffc,%eax
	subl %eax,%esp
	movl %esp,%eax
	pushl %eax
	pushl %edx
	ret
/* end */
