/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.sourcedependency.impl;

import org.eclipse.cdt.internal.core.index.IDocument;
import org.eclipse.cdt.internal.core.index.impl.IndexedFile;
import org.eclipse.cdt.internal.core.index.impl.IndexedFileHashedArray;

public class InMemoryTree {

	/**
	* hashtable of IncludeEntrys = includeFiles+numbers of the files they appear in.
	*/
	protected IncludeEntryHashedArray includes;
	/**
	 * List of IndexedFiles = file name + a unique number.
	 */
	protected IndexedFileHashedArray files;
	/**
	 * Size of the tree.
	 */
	protected long footprint;	
	private int lastId;

	public InMemoryTree() {
		init();
	}
	/**
	 * Initialises the fields of the tree
	 */
	public void init() {
		includes= new IncludeEntryHashedArray(501);
		files= new IndexedFileHashedArray(101);
		footprint= 0;
		lastId=0;
	}
	
	public IndexedFile addDocument(IDocument document) {
		IndexedFile indexedFile= this.files.add(document);
	    this.footprint += indexedFile.footprint() + 4;
		
		return indexedFile;
	}
	/**
	 * Adds the references of the include to the tree (reference = number of the file the include belongs to).
	 */
	protected void addRef(char[] include, int[] references) {
		int size= references.length;
		int i= 0;
		while (i < size) {
			if (references[i] != 0)
				addRef(include, references[i]);
			i++;
		}
	}
	/**
	 * Looks if the include already exists to the tree and adds the fileNum to this include.
	 * If the include does not exist, it adds it to the tree.
	 */
	protected void addRef(char[] include, int fileNum) {
		IncludeEntry entry= (IncludeEntry) this.includes.get(include);
		if (entry == null) {
			entry= new IncludeEntry(include, ++lastId);
			entry.addRef(fileNum);
			this.includes.add(entry);
		} else {
			this.footprint += entry.addRef(fileNum);
		}
	}

	public void addRef(IndexedFile indexedFile, char[] include) {
		addRef(include, indexedFile.getFileNumber());
	}

	public void addRef(IndexedFile indexedFile, String include) {
		addRef(include.toCharArray(), indexedFile.getFileNumber());
	}
	/**
	 * Returns the indexed file with the given path, or null if such file does not exist.
	 */
	public IndexedFile getIndexedFile(String path) {
		return files.get(path);
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
	public int getNumIncludes() {
		return includes.elementSize;
	}
	
	/**
	 * Returns the include entry corresponding to the given include.
	 */
	protected IncludeEntry getIncludeEntry(char[] include) {
		return (IncludeEntry) includes.get(include);
	}

	public void addRelatives(IndexedFile indexedFile, String inclusion, String parent) {
		addRelatives(indexedFile.getFileNumber(),inclusion.toCharArray(),(parent != null ) ? parent.toCharArray() : null);
	}
	
    protected void addRelatives(int fileNumber, char[] inclusion, char[] parent) {
		IncludeEntry childEntry=null;
		IncludeEntry parentEntry=null;
		
		if (inclusion != null)
			childEntry= (IncludeEntry) this.includes.get(inclusion);
	
		if (parent != null)
			parentEntry= (IncludeEntry) this.includes.get(parent);
		

		childEntry.addParent(fileNumber,(parentEntry!=null) ? parentEntry.getID() : -1);
		
		if (parent!=null)
			parentEntry.addChild(fileNumber,(childEntry!=null) ? childEntry.getID() : -1);
	}	
	/**
	 * Returns the include entries contained in the hashtable of includes.
	 */
	public IncludeEntry[] getIncludeEntries() {
		return this.includes.asArray();
	}
	
	public IndexedFile[] getIndexedFiles(){
		return this.files.asArray();
	}
}
