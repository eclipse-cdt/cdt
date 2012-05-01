/*******************************************************************************
 * Copyright (c) 2012 Anton Gorenkov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 * 
 * -gdb-set print object [on | off]
 * 
 * When on:
 *    for GDB <= 7.4, Runtime Type Information will be used in the gdb console.
 *    for GDB >= 7.5, Runtime Type Information will be used in the debug views.
 * When off, only static type of variable is taken into account.
 * 
 * @since 4.1
 */
public class MIGDBSetPrintObject extends MIGDBSet {
	public MIGDBSetPrintObject(ICommandControlDMContext ctx, boolean enable) {
		super(ctx, new String[] {"print", "object", enable ? "on" : "off"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}
