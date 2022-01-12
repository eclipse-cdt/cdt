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
 *     Ericsson				- Modified for handling of execution contexts
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 *  <code>-exec-finish</code>
 *  <p>
 *  Asynchronous command.  Resumes the execution of the inferior program
 *  until the current function is exited.  Displays the results returned by
 *  the function.
 *  </p>
 *  <p>
 *  The <code>-exec-finish</code> command operates on the selected stack
 *  frame.  Therefore the constructor requires a stack frame context.
 *  </p>
 *
 */
public class MIExecFinish extends MICommand<MIInfo> {
	public MIExecFinish(IFrameDMContext dmc) {
		super(dmc, "-exec-finish"); //$NON-NLS-1$
	}
}
