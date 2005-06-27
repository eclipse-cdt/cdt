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
 *      -exec-interrupt
 *
 *  Asynchronous command.  Interrupts the background execution of the
 *  target.  Note how the token associated with the stop message is the one
 *  for the execution command that has been interrupted.  The token for the
 *  interrupt itself only appears in the `^done' output.  If the user is
 *  trying to interrupt a non-running program, an error message will be
 *  printed.
 * 
 */
public class MIExecInterrupt extends MICommand 
{
	public MIExecInterrupt(String miVersion) {
		super(miVersion, "-exec-interrupt"); //$NON-NLS-1$
	}
}
