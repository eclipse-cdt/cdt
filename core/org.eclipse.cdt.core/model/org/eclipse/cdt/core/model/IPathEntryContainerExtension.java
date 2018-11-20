/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.model;

import org.eclipse.core.runtime.IPath;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPathEntryContainerExtension extends IPathEntryContainer {

	/**
	 * Returns the set of entries associated with the resource
	 * and empty array if none.
	 *
	 * @param path Workspace relative path.
	 * @param typeMask type of path entries:
	 * <li><code>IPathEntry.CDT_INCLUDE</code></li>
	 * <li><code>IPathEntry.CDT_INCLUDE_FILE</code></li>
	 * <li><code>IPathEntry.CDT_MACRO_FILE</code></li>
	 * <li><code>IPathEntry.CDT_MACRO</code></li>
	 * @return IPathEntry[] - the entries or empty set if none
	 * @see IPathEntry
	 */
	IPathEntry[] getPathEntries(IPath path, int typesMask);

	/**
	 * Returns whether there are any path entries for the resource.
	 *
	 * @param path Workspace relative path.
	 */
	boolean isEmpty(IPath path);
}
