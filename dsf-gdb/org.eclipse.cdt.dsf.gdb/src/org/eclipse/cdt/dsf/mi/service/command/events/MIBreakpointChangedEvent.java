/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.events;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;

/**
 *
 */
@Immutable
public class MIBreakpointChangedEvent extends MIEvent<IBreakpointsTargetDMContext> {

	final private int no;

	public MIBreakpointChangedEvent(IBreakpointsTargetDMContext ctx, int number) {
		this(ctx, 0, number);
	}

	public MIBreakpointChangedEvent(IBreakpointsTargetDMContext ctx, int id, int number) {
		super(ctx, id, null);
		no = number;
	}

	public int getNumber() {
		return no;
	}

}
