/*******************************************************************************
 * Copyright (c) 2012 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Dmitry Kozlov       - Initial API and implementation
*******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceTargetDMContext;

/**
 * -gdb-set trace-notes "note"
 * 
 * Sets trace notes
 * @since 4.2
 */
public class MIGDBSetTraceNote extends MIGDBSet {
	public MIGDBSetTraceNote(ITraceTargetDMContext ctx, String notes) {
		super(ctx, new String[] {"trace-notes", notes}); //$NON-NLS-1$
	}
}
