/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
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
 *     Ericsson				- Modified for handling of contexts
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 *   -file-exec-file [FILE]
 *
 *   Specify the executable file to be debugged. Unlike `-file-exec-and-symbols',
 *   the symbol table is not read from this file. If used without argument, GDB
 *   clears the information about the executable file. No output is produced,
 *   except a completion notification.
 */
public class MIFileExecFile extends MICommand<MIInfo> {
	/**
	 * @since 1.1
	 */
	public MIFileExecFile(ICommandControlDMContext dmc, String file) {
		super(dmc, "-file-exec-file", null, new String[] { file }); //$NON-NLS-1$
	}

	/**
	 * @since 1.1
	 */
	public MIFileExecFile(ICommandControlDMContext dmc) {
		super(dmc, "-file-exec-file"); //$NON-NLS-1$
	}
}
