/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *      -gdb-set
 *
 *   Set an internal GDB variable.
 * 
 */
public class MIGDBSetAutoSolib extends MIGDBSet {
	public MIGDBSetAutoSolib(boolean isSet) {
		super(new String[] {"auto-solib-add", (isSet) ? "on" : "off"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
