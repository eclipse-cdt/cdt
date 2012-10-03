/*******************************************************************************
 * Copyright (c) 2012 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.command.factories.win32;

import org.eclipse.cdt.debug.mi.core.command.MIGDBSetAutoSolib;

/**
 * Suppress "set auto-solib" - returns error on Windows
 */
class WinMIGDBSetAutoSolib extends MIGDBSetAutoSolib {

	public WinMIGDBSetAutoSolib(String miVersion, boolean isSet) {
		super(miVersion, isSet);
		setOperation(""); //$NON-NLS-1$
		setOptions(new String[0]);
		setParameters(new String[0]);
	}
}
