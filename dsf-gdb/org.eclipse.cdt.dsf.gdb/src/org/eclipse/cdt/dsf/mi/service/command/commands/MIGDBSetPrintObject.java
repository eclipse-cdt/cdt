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
 * When on, gdb (7.5 and higher) displays the MI variable type and children based on RTTI.
 * When off (or gdb version 7.4 and lower), only static type of variable is taken into account.
 * 
 * @since 4.1
 */
public class MIGDBSetPrintObject extends MIGDBSet {
	public MIGDBSetPrintObject(ICommandControlDMContext ctx, boolean enable) {
		super(ctx, new String[] {"print", "object", enable ? "on" : "off"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}
