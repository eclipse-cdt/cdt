/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *     -exec-return
 *
 *  Makes current function return immediately.  Doesn't execute the
 *  inferior.  Displays the new current frame.
 * 
 */
public class MIExecReturn extends MICommand 
{
	public MIExecReturn() {
		super("-exec-return");
	}
}
