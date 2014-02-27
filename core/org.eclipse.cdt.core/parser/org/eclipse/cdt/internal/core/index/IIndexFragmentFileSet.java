/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.core.runtime.CoreException;

public interface IIndexFragmentFileSet {
	/**
	 * Returns whether the file-set contains the file of the local binding.
	 * @throws CoreException 
	 */
	boolean containsFileOfLocalBinding(IIndexFragmentBinding binding) throws CoreException;

	/**
	 * Adds the fragment file to the file-set.
	 */
	void add(IIndexFragmentFile fragFile);

	/**
	 * Removes the fragment file from the file-set.
	 */
	void remove(IIndexFragmentFile fragFile);

	/**
	 * Returns whether the file set contains the given file.
	 */
	boolean contains(IIndexFragmentFile file) throws CoreException;

	/**
	 * Searches for all names that resolve to the given binding. You can limit the result to
	 * references, declarations or definitions, or a combination of those.
	 * @param binding a binding for which names are searched for
	 * @param flags a combination of {@link IIndexFragment#FIND_DECLARATIONS}, {@link IIndexFragment#FIND_DEFINITIONS},
	 *     {@link IIndexFragment#FIND_REFERENCES} and {@link IIndexFragment#FIND_NON_LOCAL_ONLY}
	 * @return an array of names
	 * @throws CoreException
	 */
	IIndexFragmentName[] findNames(IBinding binding, int flags) throws CoreException;
}
