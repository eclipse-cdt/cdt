/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *    -break-enable ( BREAKPOINT )+
 *
 * Enable (previously disabled) BREAKPOINT(s).
 * 
 * Result:
 *  ^done
 */
public class MIBreakEnable extends MICommand
{
	public MIBreakEnable (int[] array) {
		super("-break-enable"); //$NON-NLS-1$
		if (array != null && array.length > 0) {
			String[] brkids = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				brkids[i] = Integer.toString(array[i]);
			}
			setParameters(brkids);
		} 
	}
}
