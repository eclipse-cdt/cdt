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

import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 *
 *     -exec-until [ LOCATION ]
 *
 *  Asynchronous command.  Executes the inferior until the LOCATION
 * specified in the argument is reached.  If there is no argument, the
 * inferior executes until a source line greater than the current one is
 * reached.  The reason for stopping in this case will be
 * `location-reached'.
 *
 */
public class MIExecUntil extends MICommand<MIInfo> {
	public MIExecUntil(IExecutionDMContext dmc) {
		super(dmc, "-exec-until"); //$NON-NLS-1$
	}

	public MIExecUntil(IExecutionDMContext dmc, String loc) {
		super(dmc, "-exec-until", new String[] { loc }); //$NON-NLS-1$
	}
}
