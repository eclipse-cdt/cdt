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
 *      -exec-step
 *
 *   Asynchronous command.  Resumes execution of the inferior program,
 * stopping when the beginning of the next source line is reached, if the
 * next source line is not a function call.  If it is, stop at the first
 * instruction of the called function.
 * 
 */
public class MIExecStep extends MICommand 
{
	public MIExecStep() {
		super("-exec-step"); //$NON-NLS-1$
	}
}
