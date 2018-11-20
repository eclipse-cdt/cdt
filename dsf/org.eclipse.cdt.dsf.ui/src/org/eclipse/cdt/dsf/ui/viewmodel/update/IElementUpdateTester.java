/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel.update;

import org.eclipse.jface.viewers.TreePath;

/**
 * Tester object used to determine how individual update cache
 * entries should be updated during a flush operation.
 *
 * @see IVMUpdatePolicy
 *
 * @since 1.0
 */
public interface IElementUpdateTester {

	/**
	 * Returns the flags indicating what updates should be performed on the
	 * cache entry of the given element.
	 *
	 * @param viewerInput The input to the viewer for the given cache entry.
	 * @param path The viewer tree path for the given cache entry.
	 */
	public int getUpdateFlags(Object viewerInput, TreePath path);

	/**
	 * Returns whether update represented by this tester includes another
	 * update.  For example if update A was created as a result of an element X,
	 * and update B was created for an element Y, and element X is a parent of
	 * element Y, then tester A should include tester B.  Also a tester should
	 * always include itself.
	 * <p/>
	 * This method is used to optimize the repeated flushing of the cache as
	 * it allows the cache to avoid needlessly updating the same cache entries.
	 */
	public boolean includes(IElementUpdateTester tester);

}