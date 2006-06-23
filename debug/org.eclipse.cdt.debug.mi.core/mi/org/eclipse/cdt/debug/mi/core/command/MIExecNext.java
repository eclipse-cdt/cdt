/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
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
 *     -exec-next
 *
 *  Asynchronous command.  Resumes execution of the inferior program,
 *  stopping when the beginning of the next source line is reached.
 * 
 */
public class MIExecNext extends MICommand 
{
	public MIExecNext(String miVersion) {
		super(miVersion, "-exec-next"); //$NON-NLS-1$
	}

	public MIExecNext(String miVersion, int count) {
		super(miVersion, "-exec-next", new String[] { Integer.toString(count) }); //$NON-NLS-1$
	}
}
