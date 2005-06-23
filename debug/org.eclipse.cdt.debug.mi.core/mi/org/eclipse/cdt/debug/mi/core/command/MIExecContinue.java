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
 *      -exec-continue
 * 
 *   Asynchronous command.  Resumes the execution of the inferior program
 *   until a breakpoint is encountered, or until the inferior exits.
 * 
 */
public class MIExecContinue extends MICommand 
{
	public MIExecContinue() {
		super("-exec-continue"); //$NON-NLS-1$
	}
}
