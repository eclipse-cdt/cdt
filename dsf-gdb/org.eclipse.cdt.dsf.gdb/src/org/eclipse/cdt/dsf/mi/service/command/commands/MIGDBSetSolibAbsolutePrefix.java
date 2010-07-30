/*******************************************************************************
 * Copyright (c) 2010 Mentor Graphics Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Anna Dushistova (Mentor Graphics) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
/**
 * 
 *     -gdb-set solib-absolute-prefix PATH
 * @since 4.0
 * 
 */
public class MIGDBSetSolibAbsolutePrefix extends MIGDBSet {

	public MIGDBSetSolibAbsolutePrefix(ICommandControlDMContext ctx, String prefix) {
		super(ctx, new String[] {"solib-absolute-prefix", prefix}); //$NON-NLS-1$
	}

}
