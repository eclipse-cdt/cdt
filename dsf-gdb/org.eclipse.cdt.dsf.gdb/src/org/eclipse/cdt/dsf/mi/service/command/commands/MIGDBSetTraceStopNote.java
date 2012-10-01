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
 * -gdb-set trace-stop-notes
 * 
 * Sets trace stop notes
 * @since 4.2
 */
public class MIGDBSetTraceStopNote extends MIGDBSet {
	public MIGDBSetTraceStopNote(ITraceTargetDMContext ctx, String notes) {
		super(ctx, new String[] {"trace-stop-notes", notes}); //$NON-NLS-1$
	}
}
