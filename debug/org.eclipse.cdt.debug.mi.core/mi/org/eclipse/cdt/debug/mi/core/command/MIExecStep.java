/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *      -exec-step
 *
 *   Asynchronous command.  Resumes execution of the inferior program,
 * stopping when the beginning of the next source line is reached, if the
 * next source line is not a function call.  If it is, stop at the first
 * instruction of the called function.
 * 
 */
public class MIExecStep extends MICommand 
{
	public MIExecStep() {
		super("-exec-step"); //$NON-NLS-1$
	}
}
