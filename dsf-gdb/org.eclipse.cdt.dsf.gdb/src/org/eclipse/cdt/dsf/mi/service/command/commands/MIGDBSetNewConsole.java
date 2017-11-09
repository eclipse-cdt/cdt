/*******************************************************************************
 * Copyright (c) 2017  Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Kichwa Coders - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;

/**
 * -gdb-set new-console on|off
 *
 * Set whether to start in a new console or not
 * 
 * @since 5.4
 */
public class MIGDBSetNewConsole extends MIGDBSet {

	public MIGDBSetNewConsole(IDMContext ctx, boolean isSet) {
		super(ctx, new String[] { "new-console", isSet ? "on" : "off" }); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	}
}
