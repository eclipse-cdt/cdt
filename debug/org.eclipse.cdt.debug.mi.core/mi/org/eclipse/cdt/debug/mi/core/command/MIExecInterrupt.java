/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *      -exec-interrupt
 *
 *  Asynchronous command.  Interrupts the background execution of the
 *  target.  Note how the token associated with the stop message is the one
 *  for the execution command that has been interrupted.  The token for the
 *  interrupt itself only appears in the `^done' output.  If the user is
 *  trying to interrupt a non-running program, an error message will be
 *  printed.
 * 
 */
public class MIExecInterrupt extends MICommand 
{
	public MIExecInterrupt() {
		super("-exec-interrupt");
	}
}
