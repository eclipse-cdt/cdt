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
 *     -gdb-exit
 *
 *  Exit GDB immediately.
 * 
 */
public class MIGDBExit extends MICommand 
{
	public MIGDBExit() {
		super("-gdb-exit"); //$NON-NLS-1$
	}
}
