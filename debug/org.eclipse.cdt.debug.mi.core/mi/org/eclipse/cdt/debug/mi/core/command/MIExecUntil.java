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
 *     -exec-until [ LOCATION ]
 *
 *  Asynchronous command.  Executes the inferior until the LOCATION
 * specified in the argument is reached.  If there is no argument, the
 * inferior executes until a source line greater than the current one is
 * reached.  The reason for stopping in this case will be
 * `location-reached'.
 * 
 */
public class MIExecUntil extends MICommand 
{
	public MIExecUntil() {
		super("-exec-until"); //$NON-NLS-1$
	}

	public MIExecUntil(String loc) {
		super("-exec-until", new String[]{loc}); //$NON-NLS-1$
	}
}
