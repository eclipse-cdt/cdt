/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;

/**
 * This class represents the output from an indexer to an index 
 * for a single document.
 */

public interface IIndexerOutput {
	
	public void addIndexEntry(IIndexEntry indexEntry);
	
	public IndexedFileEntry  getIndexedFile(String path); 
	public IndexedFileEntry  addIndexedFile(String path);
	
	//For Dep Tree
	public void addIncludeRef(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	public void addIncludeRef(int indexedFileNumber, String word);
	public void addRelatives(int indexedFileNumber, String inclusion, String parent);
}
