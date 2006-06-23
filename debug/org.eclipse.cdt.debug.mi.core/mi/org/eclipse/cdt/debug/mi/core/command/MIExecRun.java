/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
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
 *      -exec-run
 *
 *   Asynchronous command.  Starts execution of the inferior from the
 * beginning.  The inferior executes until either a breakpoint is
 * encountered or the program exits.
 * 
 */
public class MIExecRun extends MICommand 
{
	public MIExecRun(String miVersion) {
		super(miVersion, "-exec-run"); //$NON-NLS-1$
	}
	
	public MIExecRun(String miVersion, String[] args) {
		super(miVersion, "-exec-run", args); //$NON-NLS-1$
	}
}
