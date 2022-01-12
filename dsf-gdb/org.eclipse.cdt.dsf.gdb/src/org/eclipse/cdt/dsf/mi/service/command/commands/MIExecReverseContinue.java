/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
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

import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * Resume backwards.
 *
 * @since 2.0
 */
public class MIExecReverseContinue extends MICommand<MIInfo> {

	public MIExecReverseContinue(IExecutionDMContext dmc) {
		super(dmc, "-interpreter-exec", new String[] { "console", "reverse-continue" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
