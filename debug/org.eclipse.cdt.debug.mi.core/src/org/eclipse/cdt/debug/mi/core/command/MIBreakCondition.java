/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *    -break-after NUMBER COUNT
 *  The breakpoint number NUMBER is not in effect until it has been hit
 *  COUNT times.
 * 
 * Result:
 *  ^done
 */
public class MIBreakCondition extends MICommand
{
	public MIBreakCondition (int brknum, String expr) {
		super("-break-condition", new String[]{Integer.toString(brknum), expr});
	}
}
