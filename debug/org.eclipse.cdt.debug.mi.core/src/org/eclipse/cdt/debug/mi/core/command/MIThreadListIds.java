/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *    -thread-list-ids
 *
 * Produces a list of the currently known GDB thread ids.  At the end
 * of the list it also prints the total number of such threads.
 * 
 */
public class MIThreadListIds extends MICommand 
{
	public MIThreadListIds() {
		super("-thread-list-ids");
	}
}
