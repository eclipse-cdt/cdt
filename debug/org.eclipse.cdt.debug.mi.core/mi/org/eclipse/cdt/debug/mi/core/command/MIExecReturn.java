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
 *     -exec-return
 *
 *  Makes current function return immediately.  Doesn't execute the
 *  inferior.  Displays the new current frame.
 * 
 */
public class MIExecReturn extends MICommand 
{
	public MIExecReturn(String miVersion) {
		super(miVersion, "-exec-return"); //$NON-NLS-1$
	}

	public MIExecReturn(String miVersion, String arg) {
		super(miVersion, "-exec-run", new String[] { arg }); //$NON-NLS-1$
	}

}
