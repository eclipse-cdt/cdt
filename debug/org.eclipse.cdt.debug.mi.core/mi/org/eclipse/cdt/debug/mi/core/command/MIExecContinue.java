/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *      -exec-continue
 * 
 *   Asynchronous command.  Resumes the execution of the inferior program
 *   until a breakpoint is encountered, or until the inferior exits.
 * 
 */
public class MIExecContinue extends MICommand 
{
	public MIExecContinue() {
		super("-exec-continue"); //$NON-NLS-1$
	}
}
