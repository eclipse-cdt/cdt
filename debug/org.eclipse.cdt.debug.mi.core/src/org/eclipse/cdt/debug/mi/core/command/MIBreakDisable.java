/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *   -break-disable ( BREAKPOINT )+
 *
 * Disable the named BREAKPOINT(s).  The field `enabled' in the break
 * list is now set to `n' for the named BREAKPOINT(s).
 * 
 * Result:
 *  ^done
 */
public class MIBreakDisable extends MICommand
{
	public MIBreakDisable (int[] array) {
		super("-break-disable");
		if (array != null && array.length > 0) {
			String[] brkids = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				brkids[i] = Integer.toString(array[i]);
			}
			setParameters(brkids);
		} 
	}
}
