/*******************************************************************************
 * Copyright (c) 2013, 2014 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Dmitry Kozlov (Mentor Graphics) - Initial API and implementation
*******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceTargetDMContext;

/**
 * -gdb-set disconnected-tracing on | off
 * 
 * Sets disconnected-tracing on or off.
 * @since 4.4
 */
public class MIGDBSetDisconnectedTracing extends MIGDBSet {
	public MIGDBSetDisconnectedTracing(ITraceTargetDMContext ctx, boolean disconnectedTracing) {
		super(ctx, new String[] {"disconnected-tracing", disconnectedTracing ? "on": "off"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
