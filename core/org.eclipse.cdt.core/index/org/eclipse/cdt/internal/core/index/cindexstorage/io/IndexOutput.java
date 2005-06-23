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

import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.WordEntry;

/**
 * An indexOutput is used to write an index into a different object (a File, ...). 
 */
public abstract class IndexOutput {
	/**
	 * Adds a File to the destination.
	 */
	public abstract void addFile(IndexedFileEntry file) throws IOException;
	/**
	 * Adds a word to the destination.
	 */
	public abstract void addWord(WordEntry word) throws IOException;
	/**
	 * Closes the output, releasing the resources it was using.
	 */
	public abstract void close() throws IOException;
	/**
	 * Flushes the output.
	 */
	public abstract void flush() throws IOException;
	/**
	 * Returns the Object the output is writing to. It can be a file, another type of index, ... 
	 */
	public abstract Object getDestination();
	/**
	 * Opens the output, before writing any information.
	 */
	public abstract void open() throws IOException;
}
