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
 * -gdb-set trace-user "user"
 *
 * Sets trace user
 * @since 4.4
 */
public class MIGDBSetTraceUser extends MIGDBSet {
	public MIGDBSetTraceUser(ITraceTargetDMContext ctx, String userName) {
		super(ctx, new String[] { "trace-user", userName }); //$NON-NLS-1$
	}
}
