/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.impl;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.internal.core.index.IDocument;

/**
 * This index stores the document names in an <code>ObjectVector</code>, and the words in
 * an <code>HashtableOfObjects</code>.
 */

public class InMemoryIndex {

	/**
	* hashtable of IncludeEntrys = includeFiles+numbers of the files they appear in.
	*/
	protected IncludeEntryHashedArray includes;
	/**
	 * hashtable of WordEntrys = words+numbers of the files they appear in.
	 */
	protected WordEntryHashedArray words;
	/**
	 * List of IndexedFiles = file name + a unique number.
	 */
	protected IndexedFileHashedArray files;
	/**
	 * Size of the index.
	 */
	protected long footprint;

	private IncludeEntry[] sortedIncludeEntries;
	private WordEntry[] sortedWordEntries;
	private IndexedFile[] sortedFiles;

	private int lastId;
	
	public InMemoryIndex() {
		includes= new IncludeEntryHashedArray(501);
		init();
	}

	public IndexedFile addDocument(IDocument document) {
		IndexedFile indexedFile= this.files.add(document);
		this.footprint += indexedFile.footprint() + 4;
		this.sortedFiles = null;
		return indexedFile;
	}
	
	public void addIncludeRef(IndexedFile indexedFile, char[] include) {
		addIncludeRef(include, indexedFile.getFileNumber());
	}
	
	public void addIncludeRef(IndexedFile indexedFile, String include) {
		addIncludeRef(include.toCharArray(), indexedFile.getFileNumber());
	}
	
	/**
		 * Adds the references of the include to the tree (reference = number of the file the include belongs to).
		 */
		protected void addIncludeRef(char[] include, int[] references) {
			int size= references.length;
			int i= 0;
			while (i < size) {
				if (references[i] != 0)
					addIncludeRef(include, references[i]);
				i++;
			}
		}
		/**
		 * Looks if the include already exists to the tree and adds the fileNum to this include.
		 * If the include does not exist, it adds it to the tree.
		 */
		protected void addIncludeRef(char[] include, int fileNum) {
			IncludeEntry entry= this.includes.get(include);
			if (entry == null) {
				entry= new IncludeEntry(include, ++lastId);
				entry.addRef(fileNum);
				this.includes.add(entry);
				this.sortedIncludeEntries= null;
				//TODO: BOG FIGURE OUT FOOTPRINT
				//this.footprint += entry.getClass(); //footprint();
				//
			} else {
				this.footprint += entry.addRef(fileNum);
			}
		}

	/**
	 * Adds the references of the word to the index (reference = number of the file the word belongs to).
	 */
	protected void addRef(char[] word, int[] references) {
		int size= references.length;
		int i= 0;
		while (i < size) {
			if (references[i] != 0)
				addRef(word, references[i]);
			i++;
		}
	}
	/**
	 * Looks if the word already exists in the index and add the fileNum to this word.
	 * If the word does not exist, it adds it in the index.
	 */
	protected void addRef(char[] word, int fileNum) {
		WordEntry entry= this.words.get(word);
		if (entry == null) {
			entry= new WordEntry(word);
			entry.addRef(fileNum);
			this.words.add(entry);
			this.sortedWordEntries= null;
			this.footprint += entry.footprint();
		} else {
			this.footprint += entry.addRef(fileNum);
		}
	}

	public void addRef(IndexedFile indexedFile, char[] word) {
		addRef(word, indexedFile.getFileNumber());
	}

	public void addRef(IndexedFile indexedFile, String word) {
		addRef(word.toCharArray(), indexedFile.getFileNumber());
	}
	
	public void addRelatives(IndexedFile indexedFile, String inclusion, String parent) {
		addRelatives(indexedFile.getFileNumber(),inclusion.toCharArray(),(parent != null ) ? parent.toCharArray() : null);
	}
	
	protected void addRelatives(int fileNumber, char[] inclusion, char[] parent) {
		IncludeEntry childEntry=null;
		IncludeEntry parentEntry=null;
		
		if (inclusion != null)
			childEntry= this.includes.get(inclusion);
	
		if (parent != null)
			parentEntry= this.includes.get(parent);
		

		childEntry.addParent(fileNumber,(parentEntry!=null) ? parentEntry.getID() : -1);
		
		if (parent!=null)
			parentEntry.addChild(fileNumber,(childEntry!=null) ? childEntry.getID() : -1);
	}	
	/**
	 * Returns the footprint of the index.
	 */
	public long getFootprint() {
		return this.footprint;
	}
	/**
	 * Returns the indexed files contained in the hashtable of includes.
	 */
	public IndexedFile[] getIndexedFiles(){
		return this.files.asArray();
	}
	/**
	 * Returns the indexed file with the given path, or null if such file does not exist.
	 */
	public IndexedFile getIndexedFile(String path) {
		return files.get(path);
	}
	/**
	 * Returns the include entries contained in the hashtable of includes.
	 */
	public IncludeEntry[] getIncludeEntries() {
		return this.includes.asArray();
	}
	/**
	 * Returns the include entry corresponding to the given include.
	 */
	protected IncludeEntry getIncludeEntry(char[] include) {
		return includes.get(include);
	}
	/**
	 * @see IIndex#getNumDocuments()
	 */
	public int getNumFiles() {
		return files.size();
	}
	/**
	 * @see IIndex#getNumWords()
	 */
	public int getNumWords() {
		return words.elementSize;
	}
	
	public int getNumIncludes() {
		return includes.elementSize;
	}
	
	/**
	 * Returns the words contained in the hashtable of words, sorted by alphabetical order.
	 */
	protected IndexedFile[] getSortedFiles() {
		if (this.sortedFiles == null) {
			IndexedFile[] indexedFiles= files.asArray();
			Util.sort(indexedFiles);
			this.sortedFiles= indexedFiles;
		}
		return this.sortedFiles;
	}
	/**
	 * Returns the word entries contained in the hashtable of words, sorted by alphabetical order.
	 */
	protected WordEntry[] getSortedWordEntries() {
		if (this.sortedWordEntries == null) {
			WordEntry[] words= this.words.asArray();
			Util.sort(words);
			this.sortedWordEntries= words;
		}
		return this.sortedWordEntries;
	}
	/**
	 * Returns the include entries contained in the hashtable of includeas, sorted by alphabetical order.
	 */
	protected IncludeEntry[] getSortedIncludeEntries() {
		if (this.sortedIncludeEntries == null) {
			IncludeEntry[] includes= this.includes.asArray();
			Util.sort(includes);
			this.sortedIncludeEntries= includes;
		}
		return this.sortedIncludeEntries;
	}
	/**
	 * Returns the word entry corresponding to the given word.
	 */
	protected WordEntry getWordEntry(char[] word) {
		return words.get(word);
	}
	/**
	 * Initialises the fields of the index
	 */
	public void init() {
		includes= new IncludeEntryHashedArray(501);
		words= new WordEntryHashedArray(501);
		files= new IndexedFileHashedArray(101);
		footprint= 0;
		lastId=0;
		sortedWordEntries= null;
		sortedFiles= null;
		sortedIncludeEntries=null;
	}
	/**
	 * Saves the index in the given file.
	 * Structure of the saved Index :
	 *   - IndexedFiles in sorted order.
	 *		+ example: 
	 *			"c:/com/a.cpp 1"
	 *			"c:/com/b.cpp 2"
	 *   - References with the words in sorted order
	 *		+ example: 
	 *			"classDecl/a 1"
	 *			"classDecl/b 2"
	 *			"ref/String 1 2"
	 */
	public void save(File file) throws IOException {
		BlocksIndexOutput output= new BlocksIndexOutput(file);
		save(output);
	}
	/**
	 * Saves the index in the given IndexOutput.
	 * Structure of the saved Index :
	 *   - IndexedFiles in sorted order.
	 *		+ example: 
	 *			"c:/com/a.cpp 1"
	 *			"c:/com/b.cpp 2"
	 *   - References with the words in sorted order
	 *		+ example: 
	 *			"classDecl/a 1"
	 *			"classDecl/b 2"
	 *			"ref/String 1 2"
	 */
	protected void save(IndexOutput output) throws IOException {
		boolean ok= false;
		try {
			output.open();
			IndexedFile[] indexedFiles= files.asArray();
			for (int i= 0, length = indexedFiles.length; i < length; ++i)
				output.addFile(indexedFiles[i]); // written out in order BUT not alphabetical
			getSortedWordEntries(); // init the slot
			for (int i= 0, numWords= sortedWordEntries.length; i < numWords; ++i)
				output.addWord(sortedWordEntries[i]);
			output.flush();
			output.close();
			ok= true;
		} finally {
			if (!ok && output != null)
				output.close();
		}
	}
}

