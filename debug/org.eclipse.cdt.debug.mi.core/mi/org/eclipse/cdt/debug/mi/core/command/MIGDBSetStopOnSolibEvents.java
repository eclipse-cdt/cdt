/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
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
 *      -gdb-set stop-on-solib-events
 *
 *   Set an internal GDB variable.
 * 
 */
public class MIGDBSetStopOnSolibEvents extends MIGDBSet {

	public MIGDBSetStopOnSolibEvents(String miVersion, boolean isSet) {
		super(miVersion, new String[] {"stop-on-solib-events", (isSet) ? "1" : "0"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
