/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

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
		super("-break-disable"); //$NON-NLS-1$
		if (array != null && array.length > 0) {
			String[] brkids = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				brkids[i] = Integer.toString(array[i]);
			}
			setParameters(brkids);
		} 
	}
}
