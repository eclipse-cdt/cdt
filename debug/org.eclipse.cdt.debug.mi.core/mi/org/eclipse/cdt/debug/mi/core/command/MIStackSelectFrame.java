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
 *     -stack-select-frame FRAMENUM
 *
 *  Change the current frame.  Select a different frame FRAMENUM on the
 * stack.
 * 
 */
public class MIStackSelectFrame extends MICommand 
{
	public MIStackSelectFrame(int frameNum) {
		super("-stack-select-frame", new String[]{Integer.toString(frameNum)}); //$NON-NLS-1$
	}
}
