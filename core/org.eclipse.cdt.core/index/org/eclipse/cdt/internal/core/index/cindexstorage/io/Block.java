/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.index.cindexstorage.io;

import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * A block is a container that can hold information (a list of file names, a list of
 * words, ...), be saved on the disk and loaded in memory.
 */

public abstract class Block {
	/**
	 * Size of the block
	 */
	protected int blockSize;

	/**
	 * Field in which the information is stored
	 */
	protected Field field;

	public Block(int blockSize) {
		this.blockSize= blockSize;
		field= new Field(blockSize);
	}
	/**
	 * Empties the block.
	 */
	public void clear() {
		field.clear();
	}
	/**
	 * Flushes the block
	 */
	public void flush() {
	}
	/**
	 * Loads the block with the given number in memory, reading it from a RandomAccessFile.
	 */
	public void read(RandomAccessFile raf, int blockNum) throws IOException {
		raf.seek(blockNum * (long) blockSize);
		raf.readFully(field.buffer());
	}
	/**
	 * Writes the block in a RandomAccessFile, giving it a block number.
	 */
	public void write(RandomAccessFile raf, int blockNum) throws IOException {
		raf.seek(blockNum * (long) blockSize);
		raf.write(field.buffer());
	}
}
