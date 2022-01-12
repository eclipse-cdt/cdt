/*******************************************************************************
 * Copyright (c) 2013 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Mentor Graphics - Initial API and implementation
 * 		John Dallaway - Add methods to get the endianness and address size (Bug 225609)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIShowEndianInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * Returns the endianness of the current GDB target.
 *
 * @since 4.2
 */
public class CLIShowEndian extends MIInterpreterExecConsole<CLIShowEndianInfo> {

	private static final String SHOW_ENDIAN = "show endian"; //$NON-NLS-1$

	public CLIShowEndian(IMemoryDMContext ctx) {
		super(ctx, SHOW_ENDIAN);
	}

	@Override
	public CLIShowEndianInfo getResult(MIOutput miResult) {
		return new CLIShowEndianInfo(miResult);
	}
}
