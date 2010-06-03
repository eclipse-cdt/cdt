/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.transport;

import java.math.BigInteger;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlockExtension;

public class BufferedMemoryWriter 
{
	private IMemoryBlockExtension fBlock;
	private byte[] fBuffer;
	private int fBufferPosition = 0;
	private BigInteger fBufferStart = null;
	
	public BufferedMemoryWriter(IMemoryBlockExtension block, int bufferLength)
	{
		fBlock = block;
		fBuffer = new byte[bufferLength];
	}
	
	public void write(BigInteger address, byte[] data) throws DebugException
	{
		while(data.length > 0)
		{
			if(fBufferStart == null)
			{
				fBufferStart = address;
				int length = data.length <= fBuffer.length ? data.length : fBuffer.length;
				System.arraycopy(data, 0, fBuffer, 0, length);
				fBufferPosition = length;
				byte[] dataRemainder = new byte[data.length - length];
				System.arraycopy(data, length, dataRemainder, 0, data.length - length);
				data = dataRemainder;
				address = address.add(BigInteger.valueOf(length));
			}
			else if(fBufferStart.add(BigInteger.valueOf(fBufferPosition)).compareTo(address) != 0)
			{
				flush();
			}
			else
			{
				int availableBufferLength = fBuffer.length - fBufferPosition;
				int length = data.length <= availableBufferLength 
					? data.length : availableBufferLength;
				System.arraycopy(data, 0, fBuffer, fBufferPosition, length);
				fBufferPosition += length;
				
				byte[] dataRemainder = new byte[data.length - length];
				System.arraycopy(data, length, dataRemainder, 0, data.length - length);
				data = dataRemainder;
				address = address.add(BigInteger.valueOf(length));
			}
			
			if(fBufferPosition == fBuffer.length)
				flush();
		}
	}
	
	public void flush() throws DebugException
	{
		if(fBufferStart != null)
		{
			byte data[] = new byte[fBufferPosition];
			System.arraycopy(fBuffer, 0, data, 0, fBufferPosition);
			fBlock.setValue(fBufferStart, data);
			fBufferStart = null;
		}
	}

}


