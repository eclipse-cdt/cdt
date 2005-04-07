/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.cindexstorage.io;

import java.io.IOException;

import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IQueryResult;
import org.eclipse.cdt.internal.core.index.cindexstorage.IncludeEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.WordEntry;


/**
 * This class provides an input on an index, after it has been generated.
 * You can access all the files of an index via getNextFile(), getCurrentFile() 
 * and moveToNextFile() (idem for the word entries). 
 * The usage is the same for every subclass: creation (constructor), opening
 * (the open() method), usage, and closing (the close() method), to release the
 * data source used by this input.
 */
public abstract class IndexInput {
	protected int filePosition;
	protected WordEntry currentWordEntry;
	protected int wordPosition;
	protected IncludeEntry currentIncludeEntry;
	protected int includePosition;

	public IndexInput() {
		super();
		wordPosition= 1;
		filePosition= 1;
		includePosition= 1;
	}
	/**
	 * clears the cache of this indexInput, if it keeps track of the information already read.
	 */
	public abstract void clearCache();
	/**
	 * Closes the IndexInput. For example, if the input is on a RandomAccessFile,
	 * it calls the close() method of RandomAccessFile. 
	 */
	public abstract void close() throws IOException;
	/**
	 * Returns the current file the indexInput is pointing to in the index.
	 */
	public abstract IndexedFileEntry getCurrentFile() throws IOException;
	/**
	 * Returns the current file the indexInput is pointing to in the index.
	 */
	public WordEntry getCurrentWordEntry() throws IOException {
		if (!hasMoreWords())
			return null;
		return currentWordEntry;
	}
	/**
	 * Returns the current file the indexInput is pointing to in the index.
	 */
	public IncludeEntry getCurrentIncludeEntry() throws IOException {
		if (!hasMoreIncludes())
			return null;
		return currentIncludeEntry;
	}
	/**
	 * Returns the position of the current file the input is pointing to in the index.
	 */
	public int getFilePosition() {
		return filePosition;
	}
	/**
	 * Returns the indexedFile corresponding to the given document number in the index the input
	 * reads in, or null if such indexedFile does not exist.
	 */
	public abstract IndexedFileEntry getIndexedFile(int fileNum) throws IOException;
	/**
	 * Returns the indexedFile corresponding to the given file path in the index the input
	 * reads in (e.g. the indexedFile with the same path in this index), or null if such 
	 * indexedFile does not exist.
	 */
	public abstract IndexedFileEntry getIndexedFile(String fullpath) throws IOException;
	/**
	 * Returns the number of files in the index.
	 */
	public abstract int getNumFiles();
	/**
	 * Returns the number of unique words in the index.
	 */
	public abstract int getNumWords();
	/**
	 * Returns the number of unique words in the index.
	 */
	public abstract int getNumIncludes();
	/**
	 * Returns the Object the input is reading from. It can be an IIndex,
	 * a File, ...
	 */
	public abstract Object getSource();
	/**
	 * Returns true if the input has not reached the end of the index for the files.
	 */
	public boolean hasMoreFiles() {
		return getFilePosition() <= getNumFiles();
	}
	/**
	 * Returns true if the input has not reached the end of the index for the files.
	 */
	public boolean hasMoreWords() {
		return wordPosition <= getNumWords();
	}
	/**
	 * Returns true if the input has not reached the end of the index for the files.
	 */
	public boolean hasMoreIncludes() {
		return includePosition <= getNumIncludes();
	}
	/**
	 * Moves the pointer on the current file to the next file in the index.
	 */
	public abstract void moveToNextFile() throws IOException;
	/**
	 * Moves the pointer on the current word to the next file in the index.
	 */
	public abstract void moveToNextWordEntry() throws IOException;
	/**
	 * Moves the pointer on the current include entry to the next file in the index.
	 */
	public abstract void moveToNextIncludeEntry() throws IOException;
	/**
	 * Open the Source where the input gets the information from.
	 */
	public abstract void open() throws IOException;
	/**
	 * Returns the list of the files containing the given word in the index.
	 */
	public abstract IQueryResult[] query(String word) throws IOException;
	public abstract IEntryResult[] queryEntriesPrefixedBy(char[] prefix) throws IOException;
	public abstract IQueryResult[] queryFilesReferringToPrefix(char[] prefix) throws IOException;
	/**
	 * Returns the list of the files whose name contain the given word in the index.
	 */
	public abstract IQueryResult[] queryInDocumentNames(String word) throws IOException;
	/**
	 * Returns the list of the files whose name contain the given word in the index.
	 */
	public abstract IncludeEntry[] queryIncludeEntries();
	/**
	 * @param fileNum
	 * @return
	 */
	public abstract IncludeEntry[] queryIncludeEntries(int fileNum) throws IOException;
	/**
	 * Set the pointer on the current file to the first file of the index.
	 */
	protected abstract void setFirstFile() throws IOException;
	/**
	 * Set the pointer on the current word to the first word of the index.
	 */
	protected abstract void setFirstWord() throws IOException;
	/**
	 * Set the pointer on the current include to the first include of the index.
	 */
	protected abstract void setFirstInclude() throws IOException;

	
}
