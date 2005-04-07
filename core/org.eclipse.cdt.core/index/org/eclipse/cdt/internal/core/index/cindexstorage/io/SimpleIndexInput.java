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
import java.util.ArrayList;

import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IQueryResult;
import org.eclipse.cdt.internal.core.index.cindexstorage.InMemoryIndex;
import org.eclipse.cdt.internal.core.index.cindexstorage.IncludeEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.WordEntry;

/**
 * A simpleIndexInput is an input on an in memory Index. 
 */

public class SimpleIndexInput extends IndexInput {
	protected WordEntry[] sortedWordEntries;
	protected IncludeEntry[] sortedIncludes;
	protected IndexedFileEntry currentFile;
	protected IndexedFileEntry[] sortedFiles;
	protected InMemoryIndex index;

	public SimpleIndexInput(InMemoryIndex index) {
		super();
		this.index= index;
	}
	/**
	 * @see IndexInput#clearCache()
	 */
	public void clearCache() {
	}
	/**
	 * @see IndexInput#close()
	 */
	public void close() throws IOException {
		sortedFiles= null;
	}
	/**
	 * @see IndexInput#getCurrentFile()
	 */
	public IndexedFileEntry getCurrentFile() throws IOException {
		if (!hasMoreFiles())
			return null;
		return currentFile;
	}
	/**
	 * @see IndexInput#getIndexedFile(int)
	 */
	public IndexedFileEntry getIndexedFile(int fileNum) throws IOException {
		for (int i= 0; i < sortedFiles.length; i++)
			if (sortedFiles[i].getFileID() == fileNum)
				return sortedFiles[i];
		return null;
	}
	/**
	 * @see IndexInput#getIndexedFile(String)
	 */
	public IndexedFileEntry getIndexedFile(String fullPath) throws IOException {
		for (int i= index.getNumFiles(); i >= 1; i--) {
			IndexedFileEntry file= getIndexedFile(i);
			if (fullPath.equals(file.getPath()))
				return file;
		}
		return null;
	}
	/**
	 * @see IndexInput#getNumFiles()
	 */
	public int getNumFiles() {
		return index.getNumFiles();
	}
	/**
	 * @see IndexInput#getNumIncludes()
	 */
	public int getNumIncludes() {
		return sortedIncludes.length;
	}
	/**
	 * @see IndexInput#getNumWords()
	 */
	public int getNumWords() {
		return sortedWordEntries.length;
	}
	/**
	 * @see IndexInput#getSource()
	 */
	public Object getSource() {
		return index;
	}
	public void init() {
		index.init();

	}
	/**
	 * @see IndexInput#moveToNextFile()
	 */
	public void moveToNextFile() throws IOException {
		filePosition++;
		if (!hasMoreFiles()) {
			return;
		}
		currentFile= sortedFiles[filePosition - 1];
	}
	/**
	 * @see IndexInput#moveToNextWordEntry()
	 */
	public void moveToNextWordEntry() throws IOException {
		wordPosition++;
		if (hasMoreWords())
			currentWordEntry= sortedWordEntries[wordPosition - 1];
	}
	/**
	 * @see IndexInput#moveToNextIncludeEntry()
	 */
	public void moveToNextIncludeEntry() throws IOException {
		includePosition++;
		if (hasMoreIncludes())
			currentIncludeEntry= sortedIncludes[includePosition - 1];
	}
	/**
	 * @see IndexInput#open()
	 */
	public void open() throws IOException {
		sortedWordEntries= index.getSortedWordEntries();
		sortedFiles= index.getSortedFiles();
		sortedIncludes = index.getSortedIncludeEntries();
		filePosition= 1;
		wordPosition= 1;
		includePosition=1;
		setFirstFile();
		setFirstWord();
		setFirstInclude();
	}
	/**
	 * @see IndexInput#query(String)
	 */
	public IQueryResult[] query(String word) throws IOException {
		char[] wordChar= word.toCharArray();
		WordEntry wordEntry= index.getWordEntry(wordChar);
		int[] fileNums= wordEntry.getRefs();
		IQueryResult[] files= new IQueryResult[fileNums.length];
		for (int i= 0; i < files.length; i++)
			files[i]= getIndexedFile(fileNums[i]);
		return files;
	}
	public IEntryResult[] queryEntriesPrefixedBy(char[] prefix) throws IOException {
		return null;
	}
	public IQueryResult[] queryFilesReferringToPrefix(char[] prefix) throws IOException {
			return null;
	}
	/**
	 * @see IndexInput#queryInDocumentNames(String)
	 */
	public IQueryResult[] queryInDocumentNames(String word) throws IOException {
		setFirstFile();
		ArrayList matches= new ArrayList();
		while (hasMoreFiles()) {
			IndexedFileEntry file= getCurrentFile();
			if (file.getPath().indexOf(word) != -1)
				matches.add(file.getPath());
			moveToNextFile();
		}
		IQueryResult[] match= new IQueryResult[matches.size()];
		matches.toArray(match);
		return match;
	}
	/**
	 * @see IndexInput#setFirstFile()
	 */
	protected void setFirstFile() throws IOException {
		filePosition= 1;
		if (sortedFiles.length > 0) {
			currentFile= sortedFiles[0];
		}
	}
	/**
	 * @see IndexInput#setFirstWord()
	 */
	protected void setFirstWord() throws IOException {
		wordPosition= 1;
		if (sortedWordEntries.length > 0)
			currentWordEntry= sortedWordEntries[0];
	}
	/**
	 * @see IndexInput#setFirstInclude()
	 */
	protected void setFirstInclude() throws IOException {
		includePosition=1;
		if (sortedIncludes.length >0)
			currentIncludeEntry=sortedIncludes[0];
	}
	public IncludeEntry[] queryIncludeEntries() {
		return null;
	}
	public IncludeEntry[] queryIncludeEntries(int fileNum) throws IOException {
		return null;
	}
}

