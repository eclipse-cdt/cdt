/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
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

import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceTargetDMContext;

/**
 * -trace-find frame-number FRAME
 *
 * @since 3.0
 */
public class MITraceFindFrameNumber extends MITraceFind {
	public MITraceFindFrameNumber(ITraceTargetDMContext ctx, int frameReference) {
		super(ctx, new String[] { "frame-number", Integer.toString(frameReference) }); //$NON-NLS-1$
	}
}
