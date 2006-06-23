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
 *    -target-select TYPE PARAMETERS ...
 *
 *  Connect GDB to the remote target.  This command takes two args:
 *
 * `TYPE'
 *     The type of target, for instance `async', `remote', etc.
 *
 * `PARAMETERS'
 *   Device names, host names and the like.  *Note Commands for
 *   managing targets: Target Commands, for more details.
 *
 * The output is a connection notification, followed by the address at
 * which the target program is, in the following form:
 *
 *   ^connected,addr="ADDRESS",func="FUNCTION NAME",
 *     args=[ARG LIST]
 * 
 */
public class MITargetSelect extends MICommand 
{
	public MITargetSelect(String miVersion, String[] params) {
		super(miVersion, "-target-select", params); //$NON-NLS-1$
	}
}
