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
public class MITargetAttach extends MICommand 
{
	public MITargetAttach(int pid) {
		super("-target-attach", new String[]{Integer.toString(pid)});
	}
}
