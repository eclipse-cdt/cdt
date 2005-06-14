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
package org.eclipse.cdt.internal.core.index;

import org.eclipse.core.resources.IFile;

/**
 * An <code>IIndexer</code> indexes ONE document at each time. It adds the document names and
 * the words references to an IIndex. Each IIndexer can index certain types of document, and should
 * not index the other files. 
 */
public interface IIndexer {
	/**
	 * Indexes the given file, adding the file name and the word references 
	 * to this document to the given <code>IIndex</code>.The caller should use 
	 * <code>shouldIndex()</code> first to determine whether this indexer handles 
	 * the given type of file, and only call this method if so. 
	 */

	void index(IFile document, IIndexerOutput output) throws java.io.IOException;

	/**
	 * Returns whether the <code>IIndexer</code> can index the given IFile or not.
	 */

	public boolean shouldIndex(IFile file);
}
