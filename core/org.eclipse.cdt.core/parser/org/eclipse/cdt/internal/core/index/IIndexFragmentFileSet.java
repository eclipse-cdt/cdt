/*******************************************************************************
 * Copyright (c) 2008, 2017 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import org.eclipse.core.runtime.CoreException;

public interface IIndexFragmentFileSet {
	/**
	 * Returns whether the file-set contains the file of the local binding.
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
	 * Returns {@code true} if this file set is empty.
	 */
	boolean isEmpty();
}
