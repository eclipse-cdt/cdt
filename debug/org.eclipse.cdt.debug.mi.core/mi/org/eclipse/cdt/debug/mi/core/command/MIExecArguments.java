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
 *      -exec-arguments ARGS
 *
 *   Set the inferior program arguments, to be used in the next
 *  `-exec-run'.
 * 
 */
public class MIExecArguments extends MICommand 
{
	public MIExecArguments(String[] args) {
		super("-exec-arguments", args); //$NON-NLS-1$
	}
}
