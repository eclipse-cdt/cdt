/*******************************************************************************
 * Copyright (c) 2008, 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 *
 * -gdb-set stop-on-solib-events
 *
 * Controls whether GDB should give you control when the dynamic linker
 * notifies it about some shared library event. The most common event of interest is loading
 * or unloading of a new shared library.
 *
 * @since 1.1
 */
public class MIGDBSetStopOnSolibEvents extends MIGDBSet {
	public MIGDBSetStopOnSolibEvents(ICommandControlDMContext ctx, boolean isSet) {
		super(ctx, new String[] { "stop-on-solib-events", (isSet) ? "1" : "0" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}