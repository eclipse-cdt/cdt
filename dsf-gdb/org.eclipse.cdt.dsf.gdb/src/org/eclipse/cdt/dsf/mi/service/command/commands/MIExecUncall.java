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

import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * Steps backward until the line where the current method
 * was originally called.
 *
 * @since 2.0
 */
public class MIExecUncall extends MICommand<MIInfo> {

	public MIExecUncall(IFrameDMContext dmc) {
		super(dmc, "-interpreter-exec", new String[] { "console", "reverse-finish" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
