/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *   -break-delete ( BREAKPOINT )+
 *
 * Delete the breakpoint(s) whose number(s) are specified in the
 * argument list.  This is obviously reflected in the breakpoint list.
 * 
 * Result:
 *  ^done
 *
 */
public class MIBreakDelete extends MICommand
{
	public MIBreakDelete (int[] array) {
		super("-break-delete"); //$NON-NLS-1$
		if (array != null && array.length > 0) {
			String[] brkids = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				brkids[i] = Integer.toString(array[i]);
			}
			setParameters(brkids);
		} 
	}
}
