/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.core.index;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.index.IndexFileSet;
import org.eclipse.core.runtime.CoreException;

/**
 * File set for index files. Can be used to filter file-local bindings.
 * @since 5.0
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IIndexFileSet {
	IIndexFileSet EMPTY = new IndexFileSet();
	
	/**
	 * Returns whether the given file is part of this file set.
	 * @since 5.1
	 */
	boolean contains(IIndexFile file) throws CoreException;

	/**
	 * Returns <code>true</code> if this file set contains a declaration or definition of
	 * the given binding.
	 * @since 5.1
	 */
	boolean containsDeclaration(IIndexBinding binding);

	/**
	 * Returns an array of bindings where all local bindings that are not part of this file-set
	 * have been removed.
	 */
	IBinding[] filterFileLocalBindings(IBinding[] bindings);	

	/**
	 * Returns an index file set with the inverse meaning.
	 * @since 5.3
	 */
	IIndexFileSet invert();

	/**
	 * Adds a file to this set.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	void add(IIndexFile indexFile);
}
