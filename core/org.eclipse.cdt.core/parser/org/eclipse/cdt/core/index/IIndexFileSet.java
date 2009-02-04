/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.index;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.index.IndexFileSet;

/**
 * File set for index files. Can be used to filter file-local bindings.
 * @since 5.0
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IIndexFileSet {
	IIndexFileSet EMPTY = new IndexFileSet();

	/**
	 * Returns an array of bindings where all local bindings that are not part of this file-set
	 * have been removed.
	 */
	IBinding[] filterFileLocalBindings(IBinding[] bindings);

	/**
	 * Adds a file to this set.
	 * @param indexFile
	 */
	void add(IIndexFile indexFile);
}
