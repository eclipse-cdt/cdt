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
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.internal.core.index.IDocument;
import org.eclipse.cdt.internal.core.index.impl.IndexedFile;
import org.eclipse.cdt.internal.core.sourcedependency.impl.InMemoryTree;


public class PreprocessorOutput implements IPreprocessorOutput {
	protected InMemoryTree tree;
	protected IndexedFile indexedFile;
	protected IDocument document;
	
	public PreprocessorOutput(InMemoryTree tree) {
		this.tree = tree;
	}

	public void addInclude(IASTInclusion inclusion, IASTInclusion parent){
		addRef(inclusion.getFullFileName());
		addRelatives(inclusion.getFullFileName(),(parent != null ) ? parent.getFullFileName() : null);
	}
	
	public void addRelatives(String inclusion, String parent) {
		if (indexedFile == null) {
					throw new IllegalStateException();
		}
		tree.addRelatives(indexedFile, inclusion, parent);	
	}

	public void addDocument(IDocument document) {
		if (indexedFile == null) {
			indexedFile= tree.addDocument(document);
		} else {
			throw new IllegalStateException();
		}
	}

	public void addRef(char[] word) {
		if (indexedFile == null) {
			throw new IllegalStateException();
		}
			tree.addRef(indexedFile, word);	
	}

	public void addRef(String word) {
		addRef(word.toCharArray());
	}
}
