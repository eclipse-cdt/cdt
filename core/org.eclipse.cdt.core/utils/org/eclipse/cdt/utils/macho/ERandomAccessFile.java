/*******************************************************************************
 * Copyright (c) 2002, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.utils.macho;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ERandomAccessFile extends RandomAccessFile {
	private boolean isle;
    private long    ptr_offset;
    int val[] = new int[4];
		
	public ERandomAccessFile(String file, String mode) throws IOException {
		super(file, mode);
	}

	public ERandomAccessFile(File file, String mode) throws IOException {
		super(file, mode);
	}

	public void setEndian(boolean le)
	{
		isle = le;
	}
	
	public final short readShortE() throws IOException {
		val[0] = read();
		val[1] = read();
		if ((val[0] | val[1]) < 0)
		    throw new EOFException();
   		if ( isle ) {
	 		return (short)((val[1] << 8) + val[0]);
		}
		return (short)((val[0] << 8) + val[1]);
	}
	
	public final int readIntE() throws IOException
	{
		val[0] = read();
		val[1] = read();
		val[2] = read();
		val[3] = read();
		if ((val[0] | val[1] | val[2] | val[3]) < 0)
		    throw new EOFException();
		if ( isle ) {
			return ((val[3] << 24) + (val[2] << 16) + (val[1] << 8) + val[0]);
		}
		return ((val[0] << 24) + (val[1] << 16) + (val[2] << 8) + val[3]);
	}

	public final long readLongE() throws IOException
	{
		return readIntE();
	}
	
    public void setFileOffset( long offset ) throws IOException {
        ptr_offset = offset;
        super.seek( offset );
    }

    public long getFilePointer() throws IOException {
        long ptr = super.getFilePointer();
        ptr = ptr - ptr_offset;
        return ptr;
    }

    public void seek( long pos ) throws IOException {
        long real_pos = pos + ptr_offset;
        super.seek( real_pos );
    }
}


