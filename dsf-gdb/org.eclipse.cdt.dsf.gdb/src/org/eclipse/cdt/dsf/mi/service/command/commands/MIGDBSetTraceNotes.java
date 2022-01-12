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
 * -gdb-set trace-notes "note"
 *
 * Sets trace notes
 * @since 4.4
 */
public class MIGDBSetTraceNotes extends MIGDBSet {
	public MIGDBSetTraceNotes(ITraceTargetDMContext ctx, String notes) {
		super(ctx, new String[] { "trace-notes", notes }); //$NON-NLS-1$
	}
}
