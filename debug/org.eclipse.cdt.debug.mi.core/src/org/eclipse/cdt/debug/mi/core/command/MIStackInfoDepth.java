/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *     -stack-info-depth [ MAX-DEPTH ]
 *
 *  Return the depth of the stack.  If the integer argument MAX-DEPTH is
 *  specified, do not count beyond MAX-DEPTH frames.
 * 
 */
public class MIStackInfoDepth extends MICommand 
{
	public MIStackInfoDepth() {
		super("-stack-info-depth");
	}

	public MIStackInfoDepth(int maxDepth) {
		super("-stack-info-depth", new String[]{Integer.toString(maxDepth)});
	}
}
