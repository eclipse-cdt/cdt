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
package org.eclipse.cdt.internal.core.index.impl;

import org.eclipse.cdt.internal.core.index.IDocument;
import org.eclipse.cdt.internal.core.index.IIndexerOutput;

/**
 * An indexerOutput is used by an indexer to add documents and word references to
 * an inMemoryIndex. It keeps track of the document being indexed and add the
 * word references to this document (so you do not need to precise the document
 * each time you add a word).
 */

public class IndexerOutput implements IIndexerOutput {
	protected InMemoryIndex index;
	protected IndexedFile indexedFile;
	protected IDocument document;
	/**
	 * IndexerOutput constructor comment.
	 */
	public IndexerOutput(InMemoryIndex index) {
		this.index= index;
	}
	/**
	 * Adds the given document to the inMemoryIndex.
	 */
	public void addDocument(IDocument document) {
		if (indexedFile == null) {
			indexedFile= index.addDocument(document);
		} else {
			throw new IllegalStateException();
		}
	}
	/**
	 * Adds a reference to the given word to the inMemoryIndex.
	 */
	public void addRef(char[] word, int indexFlags) {
		if (indexedFile == null) {
			throw new IllegalStateException();
		}
		index.addRef(indexedFile, word, indexFlags);
	}
	/**
	 * Adds a reference to the given word to the inMemoryIndex.
	 */
	public void addRef(String word, int indexFlags) {
		addRef(word.toCharArray(), indexFlags);
	}
		
	public void addRelatives(String inclusion, String parent) {
		if (indexedFile == null) {
					throw new IllegalStateException();
		}
		index.addRelatives(indexedFile, inclusion, parent);	
	}

	public void addIncludeRef(char[] word) {
		if (indexedFile == null) {
			throw new IllegalStateException();
		}
			index.addIncludeRef(indexedFile, word);	
	}

	public void addIncludeRef(String word) {
		addIncludeRef(word.toCharArray());
	}
	
	public IndexedFile getIndexedFile(String path) {
		return index.getIndexedFile(path);
	}
	
	/**
	 * Adds a file to the InMemoryIndex but does not supplant the current
	 * file being indexed. This method is to be used if the current file being indexed
	 * needs to make reference to a file that has not been added to the index as of yet.
	 */
	public IndexedFile addSecondaryIndexedFile(IDocument document) {
		return index.addDocument(document);
	}
	
	/**
	 * Adds a file to the InMemoryIndex but does not supplant the current
	 * file being indexed. This method is to be used if the current file being indexed
	 * needs to make reference to an external file that has not been added to the index as of yet.
	 */
	public IndexedFile addSecondaryExternalIndexedFile(String path) {
		return index.addExternalFilePath(path);
	}
	
}
