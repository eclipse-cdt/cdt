/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

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
		super("-break-after",new String[]{Integer.toString(brknum),
		 	 Integer.toString(count)});;
		 
	}
}
