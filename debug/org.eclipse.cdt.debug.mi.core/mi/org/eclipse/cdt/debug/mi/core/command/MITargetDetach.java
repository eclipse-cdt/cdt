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
 *
 *     -target-detach
 *
 *  Disconnect from the remote target.  There's no output.
 * 
 */
public class MITargetDetach extends MICommand 
{
	public MITargetDetach(String miVersion) {
		super(miVersion, "-target-detach"); //$NON-NLS-1$
	}
}
