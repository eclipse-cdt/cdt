/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

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
