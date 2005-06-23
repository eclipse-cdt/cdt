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
 *
 *     -target-attach PID | FILE
 *
 *  Attach to a process PID or a file FILE outside of GDB.
 * 
 */
public class MITargetAttach extends CLICommand 
{
	public MITargetAttach(int pid) {
		super("attach " + Integer.toString(pid)); //$NON-NLS-1$
	}
}
