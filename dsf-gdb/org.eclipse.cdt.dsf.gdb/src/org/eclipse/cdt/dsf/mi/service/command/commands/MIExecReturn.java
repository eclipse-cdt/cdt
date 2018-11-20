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

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 *
 *  -exec-return [arg]
 *
 *  <p>
 *  Makes current function return immediately.  Doesn't execute the
 *  inferior.  Displays the new current frame.
 *  </p>
 *  <p>
 *  The <code>-exec-return</code> command operates on the selected stack
 *  frame.  Therefore the constructor requires a stack frame context.
 *  </p>
 *
 */
public class MIExecReturn extends MICommand<MIInfo> {
	public MIExecReturn(IFrameDMContext dmc) {
		super(dmc, "-exec-return"); //$NON-NLS-1$
	}

	public MIExecReturn(IFrameDMContext dmc, String arg) {
		super(dmc, "-exec-return", new String[] { arg }); //$NON-NLS-1$
	}
}
