/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.model;

import org.eclipse.core.runtime.IPath;

/**
 * 
 */
public interface IPathEntryContainerExtension extends IPathEntryContainer {

	/**
	 * Returns the set of include entries associated with the resource
	 * and empty array if none.
	 *
	 * @param path Workspace relative path.
	 * @return IIncludeEntry[] - the entries or empty set if none
	 * @see IPathEntry
	 */
	IIncludeEntry[] getIncludeEntries(IPath path);

	/**
	 * Returns the set of macro entries associated with the resource
	 * and empty array if none.
	 * 
	 * @param path Workspace relative path.
	 * @return IMacroEntry[] - the entries or empty set if none
	 * @see IPathEntry
	 */
	IMacroEntry[] getMacroEntries(IPath path);

	/**
	 * 
	 * @param listener
	 */
	void addContainerListener(IPathEntryContainerExtensionListener listener);

	/**
	 * 
	 * @param listener
	 */
	void removeContainerListener(IPathEntryContainerExtensionListener listener);
}
