/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *
 *     -target-attach PID | FILE
 *
 *  Attach to a process PID or a file FILE outside of GDB.
 * 
 */
public class MITargetAttach extends CLICommand 
{
	public MITargetAttach(int pid) {
		super("attach " + Integer.toString(pid)); //$NON-NLS-1$
	}
}
