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
package org.eclipse.cdt.internal.core.index.cindexstorage;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.internal.core.index.cindexstorage.io.BlocksIndexOutput;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.IndexOutput;

 
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
	 * Array of WordEntry
	 */
	protected WordEntryHashedArray words;
	/**
	 * Array of IndexedFileEntry
	 */
	protected IndexedFileEntryHashedArray files;
	/**
	 * Array of IndexedPathVariableEntry = file name + a unique number.
	 */
	protected IndexPathVariableEntryHashedArray  pathVars;
	/**
	 * Size of the index.
	 */
	protected long footprint;

	private IncludeEntry[] sortedIncludeEntries;
	private WordEntry[] sortedWordEntries;
	private IndexedFileEntry[] sortedFiles;
	private IndexPathVariableEntry[] sortedPathVars;
	private int lastId;
	
	public InMemoryIndex() {
		init();
	}
	
	public IndexedFileEntry addFile(String path){
	    IndexedFileEntry indexedFileEntry = this.files.add(path);
		this.footprint += indexedFileEntry.footprint() + 4;
		this.sortedFiles = null;
		return indexedFileEntry;
	}
	
	public void addIncludeRef(IndexedFileEntry indexedFile, char[] include) {
		addIncludeRef(include, indexedFile.getFileID());
	}
	
	public void addIncludeRef(IndexedFileEntry indexedFile, String include) {
		addIncludeRef(include.toCharArray(), indexedFile.getFileID());
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
			this.footprint += entry.footprint();
		} else {
			this.footprint += entry.addRef(fileNum);
		}
	}
	/**
	 * Looks if the word already exists in the index and add the fileNum to this word.
	 * If the word does not exist, it adds it in the index.
	 * @param indexFlags
	 */
	protected void addRef(char[] word, int fileNum, int offset, int offsetType) {
		WordEntry entry= this.words.get(word);
	
		if (entry == null) {
			entry= new WordEntry(word);
			entry.addRef(fileNum);
			entry.addOffset(offset,fileNum, offsetType);
			this.words.add(entry);
			this.sortedWordEntries= null;
			this.footprint += entry.footprint();
		} else {
			this.footprint += entry.addRef(fileNum);
			entry.addOffset(offset, fileNum, offsetType);
		}
	}

	public void addRef(IndexedFileEntry indexedFile, char[] word, int offset, int offsetType) {
		addRef(word, indexedFile.getFileID(), offset, offsetType);
	}

	public void addRef(IndexedFileEntry indexedFile, String word, int offset, int offsetType) {
		addRef(word.toCharArray(), indexedFile.getFileID(), offset, offsetType);
	}
	
	public void addRelatives(int fileNumber, String inclusion, String parent) {
		addRelatives(fileNumber,inclusion.toCharArray(),(parent != null ) ? parent.toCharArray() : null);
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
	public IndexedFileEntry[] getIndexedFiles(){
		return this.files.asArray();
	}
	/**
	 * Returns the indexed file with the given path, or null if such file does not exist.
	 */
	public IndexedFileEntry getIndexedFile(String path) {
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

	public int getNumFiles() {
		return files.size();
	}
	
	public int getNumWords() {
		return words.elementSize;
	}
	
	public int getNumIncludes() {
		return includes.elementSize;
	}
	
	/**
	 * Returns the words contained in the hashtable of words, sorted by alphabetical order.
	 */
	public IndexedFileEntry[] getSortedFiles() {
		if (this.sortedFiles == null) {
			IndexedFileEntry[] indexedFiles= files.asArray();
			Util.sort(indexedFiles);
			this.sortedFiles= indexedFiles;
		}
		return this.sortedFiles;
	}
	/**
	 * Returns the word entries contained in the hashtable of words, sorted by alphabetical order.
	 */
	public WordEntry[] getSortedWordEntries() {
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
	public IncludeEntry[] getSortedIncludeEntries() {
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
	public WordEntry getWordEntry(char[] word) {
		return words.get(word);
	}
	/**
	 * Initialises the fields of the index
	 */
	public void init() {
		includes= new IncludeEntryHashedArray(501);
		words= new WordEntryHashedArray(501);
		files= new IndexedFileEntryHashedArray(101);
		pathVars= new IndexPathVariableEntryHashedArray(101);
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
			IndexedFileEntry[] indexedFiles= files.asArray();
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

