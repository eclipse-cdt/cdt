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

import org.eclipse.cdt.internal.core.index.IIndexerOutput;

/**
 * An indexerOutput is used by an indexer to add files and word references to
 * an inMemoryIndex. 
 */

public class IndexerOutput implements IIndexerOutput {
	protected InMemoryIndex index;
	/**
	 * IndexerOutput constructor comment.
	 */
	public IndexerOutput(InMemoryIndex index) {
		this.index= index;
	}
	
	/**
	 * @deprecated
	 */
	public void addRef(int indexedFileNumber, char[] word){
	    addRef(indexedFileNumber,word,1,1, 1);
	}
	
	/**
	 * Adds a reference to the given word to the inMemoryIndex.
	 */
	public void addRef(int indexedFileNumber, char[] word, int offset, int offsetLength, int offsetType) {
		if (indexedFileNumber == 0) {
			throw new IllegalStateException();
		}
		
		
		index.addRef(word, indexedFileNumber, offset, offsetType);
	}  
	/**
	 * Adds a reference to the given word to the inMemoryIndex.
	 */
	public void addRef(int indexedFileNumber, String word, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber, word.toCharArray(), offset, offsetLength, offsetType);
	}
		
	public void addRelatives(int indexedFileNumber, String inclusion, String parent) {
		if (indexedFileNumber == 0) {
			throw new IllegalStateException();
		}
		index.addRelatives(indexedFileNumber, inclusion, parent);	
	}

	public void addIncludeRef(int indexedFileNumber, char[] word) {
		if (indexedFileNumber == 0) {
			throw new IllegalStateException();
		}
			index.addIncludeRef(word, indexedFileNumber);	
	}

	public void addIncludeRef(int indexedFileNumber, String word) {
		addIncludeRef(indexedFileNumber, word.toCharArray());
	}
	
	public IndexedFileEntry getIndexedFile(String path) {
		return index.getIndexedFile(path);
	}
	
	/**
	 * Adds the file path to the index, creating a new file entry
	 * for it
	 */
	public IndexedFileEntry addIndexedFile(String path) {
		return index.addFile(path);
	}
	
}
