/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *     -gdb-exit
 *
 *  Exit GDB immediately.
 * 
 */
public class MIGDBExit extends MICommand 
{
	public MIGDBExit() {
		super("-gdb-exit"); //$NON-NLS-1$
	}
}
