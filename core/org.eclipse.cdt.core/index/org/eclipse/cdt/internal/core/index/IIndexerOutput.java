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
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.internal.core.index.impl.IndexedFile;

/**
 * This class represents the output from an indexer to an index 
 * for a single document.
 */

public interface IIndexerOutput {
	public void addDocument(IDocument document);
	public void addRef(char[] word, int indexFlags);
	public void addRef(String word, int indexFlags);
	public IndexedFile getIndexedFile(String path); 
	public IndexedFile addSecondaryIndexedFile(IDocument document);
	public IndexedFile addSecondaryExternalIndexedFile(String path);
	//For Dep Tree
	public void addIncludeRef(char[] word);
	public void addIncludeRef(String word);
	public void addRelatives(String inclusion, String parent);
}
