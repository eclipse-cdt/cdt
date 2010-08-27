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

package org.eclipse.cdt.dsf.mi.service.command.output;

import org.eclipse.debug.core.model.MemoryByte;

/** 
 * Example output is:
 * 
 *     (gdb)
 *     -data-read-memory-bytes &a 10
 *     ^done,memory=[{begin="0xbffff154",offset="0x00000000",
 *                    end="0xbffff15e",
 *                    contents="01000000020000000300"}]
 * @since 4.0
 */
public class MIDataReadMemoryBytesInfo extends MIInfo {
	
	/* The cached memory block.  */
	private MemoryByte[] fBlock = null;

	public MIDataReadMemoryBytesInfo(MIOutput output, int size) {
		super(output);
		parse(size);
	}
		
	/**
	 * Return the memory block
	 */
	public MemoryByte[] getMIMemoryBlock() {		
		return fBlock;		
	}
	
	private void parse(int size)
	{		
		fBlock = new MemoryByte[size];
		// Fill the block with invalid bytes, initially.
		for (int i = 0; i < size; i++)
			fBlock[i] = new MemoryByte((byte) 0, (byte) 0);

		MIResult[] results = getMIOutput().getMIResultRecord().getMIResults();

		for (int i = 0; i < results.length; i++) {

			if (results[i].getVariable().equals("memory")) //$NON-NLS-1$
			{	
				MIList v = (MIList) (results[i].getMIValue());
				try {
					for (int j = 0; j < v.getMIValues().length; ++j) {
						MITuple b = (MITuple) (v.getMIValues()[j]);
						int offset = 0;
						String contents = ""; //$NON-NLS-1$
						for (int k = 0; k < b.getMIResults().length; ++k) {
							MIResult r = b.getMIResults()[k];
							if (r.getVariable().equals("offset")) //$NON-NLS-1$
							{
								String offset_s = ((MIConst) r.getMIValue())
								.getCString();
								offset = Integer.decode(offset_s);
							} else if (r.getVariable().equals("contents")) //$NON-NLS-1$
							{
								contents = ((MIConst) r.getMIValue()).getCString();
							}
						}

						if (offset + contents.length()/2 <= size)
							for (int k = 0; k < contents.length() / 2; ++k) {
								fBlock[offset + k] = new MemoryByte(
										(byte) Integer.parseInt(
												contents.substring(k * 2, k * 2 + 2),
												16));
							}
					}
				}
				catch(NumberFormatException e)
				{
					// Something went wrong. Stop processing this memory range.
				}
			}
		}
	}

}
