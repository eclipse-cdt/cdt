/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.internal.core.sourcedependency;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.index.IDocument;
import org.eclipse.cdt.internal.core.index.IQueryResult;
import org.eclipse.core.runtime.IPath;

public interface IDependencyTree {
	/**
	 * Adds the given document to the index.
	 */
	void add(IDocument document, String docPath, IScannerInfo newInfo) throws IOException;
	/**
	 * Empties the index.
	 */
	void empty() throws IOException;
	/**
	 * Returns the index file on the disk.
	 */
	File getIndexFile();
	/**
	 * Returns the number of documents indexed.
	 */
	int getNumDocuments() throws IOException;
	/**
	 * Returns the number of unique words indexed.
	 */
	int getNumIncludes() throws IOException;
	/**
	 * Returns the path corresponding to a given document number
	 */
	String getPath(int documentNumber) throws IOException;
	/**
	 * Ansers true if has some changes to save.
	 */
	boolean hasChanged();
	/**
	 * Returns the paths of the documents containing the given word.
	 */
	IQueryResult[] query(String word) throws IOException;
	/**
	 * Returns the paths of the documents whose names contain the given word.
	 */
	IQueryResult[] queryInDocumentNames(String word) throws IOException;
		
	/**
	 * Removes the corresponding document from the tree.
	 */
	void remove(String documentName) throws IOException;
	/**
	 * Saves the index on the disk.
	 */
	void save() throws IOException;
	/**
	 * Gets the files that are included by the passed in file.
	 */
	String[] getFileDependencies(IPath filePath) throws IOException;
	//	TODO: BOG Debug Method Take out
	/**
	 * Prints all of the IncludeEntries for this project.
	 */
	public void printIncludeEntries();
	//	TODO: BOG Debug Method Take out
	/**
	 * Prints all of the IndexedFiles for this project.
	 */
	public void printIndexedFiles();
}
