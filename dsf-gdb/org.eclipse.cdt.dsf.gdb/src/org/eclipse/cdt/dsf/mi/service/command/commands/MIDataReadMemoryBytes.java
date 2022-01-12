/*******************************************************************************
 * Copyright (c) 2010, 2014 CodeSourcery and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Prus (CodeSourcery) - Initial API and implementation
 *     Alvaro Sanchez-Leon (Ericsson AB) - [Memory] Support 16 bit addressable size (Bug 426730)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataReadMemoryBytesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * -data-read-memory-bytes [ -o BYTE-OFFSET ]
 *                           ADDRESS COUNT
 * where:
 *
 * `ADDRESS'
 *     An expression specifying the address of the first memory word to be
 *     read.  Complex expressions containing embedded white space should
 *     be quoted using the C convention.
 *
 * `COUNT'
 *     The number of addressable units to read.  This should be an integer literal.
 *
 * `BYTE-OFFSET'
 *     The offset in addressable units relative to ADDRESS at which to start
 *     reading.  This should be an integer literal.  This option is
 *     provided so that a frontend is not required to first evaluate
 *     address and then perform address arithmetics itself.
 * @since 4.0
 */
public class MIDataReadMemoryBytes extends MICommand<MIDataReadMemoryBytesInfo> {

	private static final int DEFAULT_ADDRESSABLE_SIZE = 1;
	private int fCount;
	private int fword_size;

	public MIDataReadMemoryBytes(IDMContext ctx, String address, long offset, int count) {
		this(ctx, address, offset, count, DEFAULT_ADDRESSABLE_SIZE);
	}

	/**
	 * @param count - The number of addressable units to read from memory
	 * @since 4.4
	 */
	public MIDataReadMemoryBytes(IDMContext ctx, String address, long offset, int count, int word_size) {
		super(ctx, "-data-read-memory-bytes"); //$NON-NLS-1$

		fCount = count;
		fword_size = word_size;

		if (offset != 0) {
			setOptions(new String[] { "-o", Long.toString(offset) }); //$NON-NLS-1$
		}

		setParameters(new String[] { address, Integer.toString(count) });
	}

	@Override
	public MIDataReadMemoryBytesInfo getResult(MIOutput out) {
		return new MIDataReadMemoryBytesInfo(out, fCount, fword_size);
	}
}
