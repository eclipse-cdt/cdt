/*******************************************************************************
 * Copyright (c) 2006, 2013 Wind River Systems, Inc. and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.core.runtime.CoreException;

public interface IIndexFragmentInclude extends IIndexInclude {
	/**
	 * Empty array constant
	 * @since 4.0.1
	 */
	IIndexFragmentInclude[] EMPTY_FRAGMENT_INCLUDES_ARRAY = {};

	/**
	 * Returns the fragment that owns this include.
	 */
	IIndexFragment getFragment();

	/**
	 * Returns the file that is included by this include. May return {@code null} in case
	 * the included file is not part of this fragment.
	 */
	IIndexFragmentFile getIncludes() throws CoreException;
}
