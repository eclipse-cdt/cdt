/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *     -exec-next
 *
 *  Asynchronous command.  Resumes execution of the inferior program,
 *  stopping when the beginning of the next source line is reached.
 * 
 */
public class MIExecNext extends MICommand 
{
	public MIExecNext() {
		super("-exec-next"); //$NON-NLS-1$
	}
}
