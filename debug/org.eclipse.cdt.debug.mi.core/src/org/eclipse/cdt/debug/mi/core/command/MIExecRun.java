/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *      -exec-run
 *
 *   Asynchronous command.  Starts execution of the inferior from the
 * beginning.  The inferior executes until either a breakpoint is
 * encountered or the program exits.
 * 
 */
public class MIExecRun extends MICommand 
{
	public MIExecRun(String[] args) {
		super("-exec-run", args);
	}
}
