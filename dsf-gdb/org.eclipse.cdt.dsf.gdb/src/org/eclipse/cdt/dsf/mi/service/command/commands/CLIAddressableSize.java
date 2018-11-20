/*******************************************************************************
 * Copyright (c) 2014 Ericsson AB and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson AB) - [Memory] Support 16 bit addressable size (Bug 426730)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIAddressableSizeInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * <p>CLI command used to resolve the addressable size </p>
 * The minimum addressable size
 * is determined by the space used to store a "char" on a target system
 * <br>
 * This is
 * then resolved by retrieving a hex representation of -1 casted to the size of
 * a "char"
 * <br>
 * <br>E.g. from GDB command line
 * <br>
 * > p/x (char)-1 <br>
 * > $7 = 0xffff <br>
 *
 * <p>Since two hex characters are representing one octet, for the above example
 * this method should return 2</p>
 *
 * @since 4.4
 */
public class CLIAddressableSize extends MIInterpreterExecConsole<CLIAddressableSizeInfo> {

	private static final String hexOfBitsContainedInChar = "p/x (char)-1"; //$NON-NLS-1$

	public CLIAddressableSize(IMemoryDMContext ctx) {
		super(ctx, hexOfBitsContainedInChar);
	}

	@Override
	public CLIAddressableSizeInfo getResult(MIOutput miResult) {
		return new CLIAddressableSizeInfo(miResult);
	}
}
