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

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

/**
 * An IIndex is the interface used to generate an index file, and to make queries on
 * this index.
 */

public interface IIndex {
	
    //Used for offsets LINE = line based, while OFFSET = character offset
    final static int LINE=1;
    final static int OFFSET=2;
    
    // Constants used to refer to index items
	final static int ANY = 0;
	
	// All index elements can be described as a triple (meta_kind, kind, type) 
	
	// meta_kind
	final static int TYPE = 1;
	final static int FUNCTION = 2;
	final static int METHOD = 3;
	final static int FIELD = 4;
	final static int MACRO = 5;
	final static int NAMESPACE = 6;
	final static int ENUMTOR = 7;
	final static int INCLUDE = 8;
	
	// kind
	final static int TYPE_CLASS = 1;
	final static int TYPE_STRUCT = 2;
	final static int TYPE_UNION = 3;
	final static int TYPE_ENUM = 4;
	final static int TYPE_VAR = 5;
	final static int TYPE_TYPEDEF = 6;
	final static int TYPE_DERIVED = 7;
	final static int TYPE_FRIEND = 8;
	final static int TYPE_FWD_CLASS = 9;
	final static int TYPE_FWD_STRUCT = 10;
	final static int TYPE_FWD_UNION = 11;
	
	// type
    final static int UNKNOWN = 0;
    final static int DECLARATION = 1;
    final static int REFERENCE = 2;
    final static int DEFINITION = 3;
	
	
	// modifiers
	final static int privateAccessSpecifier = 1;
	final static int publicAccessSpecifier = 2;
	final static int protectedAccessSpecifier = 4;
	final static int constQualifier = 8;
	final static int volatileQualifier = 16;
	final static int staticSpecifier = 32;
	final static int externSpecifier = 64;
	final static int inlineSpecifier = 128;
	final static int virtualSpecifier = 256;
	final static int pureVirtualSpecifier = 512;
	final static int explicitSpecifier = 1024;
	final static int autoSpecifier = 2048;
	final static int registerSpecifier = 4096;
	final static int mutableSpecifier = 8192;
	
	
	   /**
		 * Adds the given file to the index.
		 */
		void add(IFile file, IIndexer indexer) throws IOException;
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
		
		String [] getDocumentList() throws IOException;
		
		int getNumDocuments() throws IOException;
		/**
		 * Returns the number of unique words indexed.
		 */
		int getNumWords() throws IOException;
		/**
		 * Returns the path corresponding to a given document number
		 */
		String getPath(int documentNumber) throws IOException;
		/**
		 * Ansers true if has some changes to save.
		 */
		boolean hasChanged();

		/**
		 * Returns all entries for a given pattern.
		 */
		
		IEntryResult[] getEntries(int meta_kind, int kind, int ref, String name) throws IOException;
		IEntryResult[] getEntries(int meta_kind, int kind, int ref) throws IOException;

		/**
		 * Returns the paths of the documents whose names contain the given word.
		 */
		IQueryResult[] queryInDocumentNames(String word) throws IOException;
		/**
		 * Returns the paths of the documents containing the given word prefix.
		 */
		
		IQueryResult[] getPrefix(int meta_kind, int kind, int ref, String name) throws IOException;
		IQueryResult[] getPrefix(int meta_kind, int kind, int ref) throws IOException;

		/**
		 * Removes the corresponding document from the index.
		 */
		void remove(String documentName) throws IOException;

		/**
		 * Saves the index on the disk.
		 */
		void save() throws IOException;
		/**
		 * @param path
		 * @return
		 */
		String[] getFileDependencies(IPath path) throws IOException;
	    String[] getFileDependencies(IFile file) throws IOException;

}
