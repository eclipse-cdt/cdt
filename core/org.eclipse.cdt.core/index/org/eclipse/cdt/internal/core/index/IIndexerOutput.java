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

	public void addEnumtorDecl(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	public void addEnumtorRef(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);

	public void addMacroDecl(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	public void addMacroRef(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);

	public void addFieldDecl(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	public void addFieldRef(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	
	public void addMethodDecl(int indexedFileNumber, char [][] name, /*char[][] parameterTypes,*/ int offset, int offsetLength, int offsetType);
	public void addMethodRef(int indexedFileNumber, char [][] name, /*char[][] parameterTypes,*/ int offset, int offsetLength, int offsetType);
	
	public void addFunctionDecl(int indexedFileNumber, char [][] name, /*char[][] parameterTypes,*/int offset, int offsetLength, int offsetType);
	public void addFunctionRef(int indexedFileNumber, char [][] name, /*char[][] parameterTypes,*/int offset, int offsetLength, int offsetType);
	
	public void addNamespaceDecl(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	public void addNamespaceRef(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	
	public void addIncludeRef(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	
	public void addStructDecl(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	public void addStructRef(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);

	public void addTypedefDecl(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	public void addTypedefRef(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	
	public void addUnionDecl(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	public void addUnionRef(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	
	public void addVariableDecl(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	public void addVariableRef(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	
	public void addClassDecl(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	public void addClassRef(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	
	public void addEnumDecl(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	public void addEnumRef(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	
	public void addDerivedDecl(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	public void addDerivedRef(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	
	public void addFriendDecl(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	public void addFriendRef(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);

	public void addFwd_ClassDecl(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	public void addFwd_ClassRef(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	
	public void addFwd_StructDecl(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	public void addFwd_StructRef(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	
	public void addFwd_UnionDecl(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	public void addFwd_UnionRef(int indexedFileNumber, char [][] name, int offset, int offsetLength, int offsetType);
	
	public IndexedFileEntry  getIndexedFile(String path); 
	public IndexedFileEntry  addIndexedFile(String path);
	//For Dep Tree
//	public void addIncludeRef(int indexedFileNumber, char[] word);
	public void addIncludeRef(int indexedFileNumber, String word);
	public void addRelatives(int indexedFileNumber, String inclusion, String parent);
}
