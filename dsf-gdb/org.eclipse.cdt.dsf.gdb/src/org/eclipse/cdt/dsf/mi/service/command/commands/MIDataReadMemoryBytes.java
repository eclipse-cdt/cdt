/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Prus (CodeSourcery) - Initial API and implementation
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
 *     The number of bytes to read.  This should be an integer literal.
 *     
 * `BYTE-OFFSET'
 *     The offsets in bytes relative to ADDRESS at which to start
 *     reading.  This should be an integer literal.  This option is
 *     provided so that a frontend is not required to first evaluate
 *     address and then perform address arithmetics itself.
 * @since 4.0
 */                       
public class MIDataReadMemoryBytes extends MICommand<MIDataReadMemoryBytesInfo> {
	
	private int fSize;

	public MIDataReadMemoryBytes(IDMContext ctx, String address, long offset,
			int num_bytes) {
		super(ctx, "-data-read-memory-bytes"); //$NON-NLS-1$
		
		fSize = num_bytes;

		if (offset != 0) {
			setOptions(new String[] { "-o", Long.toString(offset) }); //$NON-NLS-1$
		}

		setParameters(new String[] { address, Integer.toString(num_bytes) });
	}

	@Override
	public MIDataReadMemoryBytesInfo getResult(MIOutput out) {
		return new MIDataReadMemoryBytesInfo(out, fSize);
	}
}
