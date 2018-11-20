/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
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

import java.util.Collection;

import org.eclipse.jface.viewers.TreePath;

/**
 * Element update tester extension which allows an update policy to selectively
 * flush properties of elements.  This can be useful if the update tester's event
 * only affects a certain aspect of the element's presentation.
 *
 * @since 2.1
 */
public interface IElementUpdateTesterExtension extends IElementUpdateTester {

	/**
	 * Returns the properties that should be flushed for the element.
	 *
	 * @param viewerInput The input to the viewer for the given cache entry.
	 * @param path The viewer tree path for the given cache entry.
	 * @param isDirty <code>true</code> if the given cache entry is already dirty
	 * @return Collection of properties which should be flushed, or
	 * <code>null</code> if none.
	 */
	Collection<String> getPropertiesToFlush(Object viewerInput, TreePath path, boolean isDirty);
}
