/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *     -exec-finish
 *
 *  Asynchronous command.  Resumes the execution of the inferior program
 *  until the current function is exited.  Displays the results returned by
 *  the function.
 * 
 */
public class MIExecAbort extends CLICommand 
{
	public MIExecAbort() {
		super("kill"); //$NON-NLS-1$
	}
}
