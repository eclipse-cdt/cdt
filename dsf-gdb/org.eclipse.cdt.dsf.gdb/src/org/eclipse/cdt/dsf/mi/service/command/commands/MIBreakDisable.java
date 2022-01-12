/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
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

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 *
 *   -break-disable ( BREAKPOINT )+
 *
 * Disable the named BREAKPOINT(s).  The field `enabled' in the break
 * list is now set to `n' for the named BREAKPOINT(s).
 *
 * Result:
 *  ^done
 */

public class MIBreakDisable extends MICommand<MIInfo> {
	/** @since 5.0 */
	public MIBreakDisable(IBreakpointsTargetDMContext ctx, String[] array) {
		super(ctx, "-break-disable"); //$NON-NLS-1$
		if (array != null && array.length > 0) {
			String[] brkids = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				brkids[i] = array[i];
			}
			setParameters(brkids);
		}
	}
}
