/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *    -thread-select THREADNUM
 *
 * Make THREADNUM the current thread.  It prints the number of the new
 * current thread, and the topmost frame for that thread.
 * 
 */
public class MIThreadSelect extends MICommand 
{
	public MIThreadSelect(int threadNum) {
		super("-thread-select", new String[]{Integer.toString(threadNum)});
	}
}
