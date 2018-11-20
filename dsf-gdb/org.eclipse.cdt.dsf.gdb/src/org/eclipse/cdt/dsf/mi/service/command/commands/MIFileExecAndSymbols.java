/*******************************************************************************
 * Copyright (c) 2008, 2015 Ericsson and others.
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
 *     Onur Akdemir (TUBITAK BILGEM-ITI) - Multi-process debugging (Bug 237306)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * -file-exec-and-symbols [FILE]
 *
 * Specify the executable file to be debugged. This file is the one from which
 * the symbol table is also read. If no file is specified, the command clears
 * the executable and symbol information. If breakpoints are set when using this
 * command with no arguments, gdb will produce error messages. Otherwise, no
 * output is produced, except a completion notification.
 */
public class MIFileExecAndSymbols extends MICommand<MIInfo> {
	/** @since 4.0 */
	public MIFileExecAndSymbols(IMIContainerDMContext dmc) {
		this(dmc, null);
	}

	/** @since 4.0 */
	public MIFileExecAndSymbols(IMIContainerDMContext dmc, String file) {
		super(dmc, "-file-exec-and-symbols", null, file == null ? null : new String[] { file }); //$NON-NLS-1$
	}
}
