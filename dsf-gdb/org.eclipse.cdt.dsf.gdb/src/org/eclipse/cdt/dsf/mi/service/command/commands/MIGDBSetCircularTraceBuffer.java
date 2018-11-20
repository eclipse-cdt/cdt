/*******************************************************************************
 * Copyright (c) 2013, 2014 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dmitry Kozlov (Mentor Graphics) - Initial API and implementation
*******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceTargetDMContext;

/**
 * -gdb-set circular-trace-buffer on | off
 *
 * Sets circular trace buffer on or off.
 * @since 4.4
 */
public class MIGDBSetCircularTraceBuffer extends MIGDBSet {
	public MIGDBSetCircularTraceBuffer(ITraceTargetDMContext ctx, boolean useCircularTraceBuffer) {
		super(ctx, new String[] { "circular-trace-buffer", useCircularTraceBuffer ? "on" : "off" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
