/*******************************************************************************
 * Copyright (c) 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Dallaway - MIDataWriteMemoryBytes based on MIDataWriteMemory (bug 387793)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * -data-write-memory-bytes ADDRESS CONTENTS
 *
 * where:
 *
 * 'ADDRESS'
 *     An expression specifying the address of the first memory word to be
 *     written.  Complex expressions containing embedded white space should
 *     be quoted using the C convention.
 *
 * 'CONTENTS'
 *     The hex-encoded bytes to write.
 * @since 4.2
 */
public class MIDataWriteMemoryBytes extends MICommand<MIInfo> {

	public MIDataWriteMemoryBytes(
	        IDMContext ctx,
			String address,
			byte[] contents)
	{
		super(ctx, "-data-write-memory-bytes"); //$NON-NLS-1$

		// performance-oriented conversion of byte[] to hex string
		final char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
		char[] hex = new char[contents.length * 2];
		for (int n = 0; n < contents.length; n++) {
			final int val = contents[n] & 0xFF;
			hex[n*2] = digits[val >>> 4];
			hex[n*2 + 1] = digits[val & 0x0F];
		}
		setParameters(
			new String[] {
				address,
				new String(hex)});
	}
}
