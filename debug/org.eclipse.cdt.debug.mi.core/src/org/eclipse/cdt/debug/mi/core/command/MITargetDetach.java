/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *
 *     -target-detach
 *
 *  Disconnect from the remote target.  There's no output.
 * 
 */
public class MITargetDetach extends MICommand 
{
	public MITargetDetach() {
		super("-target-detach");
	}
}
