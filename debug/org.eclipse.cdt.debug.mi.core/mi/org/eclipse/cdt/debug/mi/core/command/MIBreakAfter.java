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
 * 
 */
public class MIBreakAfter extends MICommand
{
	public MIBreakAfter(int brknum, int count) {
		super("-break-after",new String[]{Integer.toString(brknum), //$NON-NLS-1$
		 	 Integer.toString(count)});
		 
	}
}
