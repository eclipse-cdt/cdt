/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceTargetDMContext;

/**
 * -trace-find none
 *
 * @since 3.0
 */
public class MITraceFindNone extends MITraceFind {
	public MITraceFindNone(ITraceTargetDMContext ctx) {
		super(ctx, new String[] { "none" }); //$NON-NLS-1$
	}
}
