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
 * -gdb-set charset CHARSET
 * 
 * Sets both the current host and target charset to CHARSET. The host charset is the
 * charset used by gdb. The target charset is the charset used by the char type of the
 * inferior program.
 * 
 * @since 4.1
 */
public class MIGDBSetCharset extends MIGDBSet {
	public MIGDBSetCharset(ICommandControlDMContext ctx, String charset) {
		super(ctx, new String[] {"charset", charset}); //$NON-NLS-1$
	}
}
