/*******************************************************************************
 * Copyright (c) 2012 Mathias Kunter and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Mathias Kunter       - Initial API and implementation
*******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 * 
 * -gdb-set host-charset CHARSET
 * 
 * Sets the current host charset to CHARSET. The host charset is the charset used by gdb.
 * 
 * @since 4.1
 */
public class MIGDBSetHostCharset extends MIGDBSet {
	public MIGDBSetHostCharset(ICommandControlDMContext ctx, String hostCharset) {
		super(ctx, new String[] {"host-charset", hostCharset}); //$NON-NLS-1$
	}
}
