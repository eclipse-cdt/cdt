/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *      -exec-next-instruction
 *
 *   Asynchronous command.  Executes one machine instruction.  If the
 * instruction is a function call continues until the function returns.  If
 * the program stops at an instruction in the middle of a source line, the
 * address will be printed as well.
 * 
 */
public class MIExecNextInstruction extends MICommand 
{
	public MIExecNextInstruction() {
		super("-exec-next-instruction");
	}
}
