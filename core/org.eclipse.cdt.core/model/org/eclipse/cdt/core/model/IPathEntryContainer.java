/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.model;

import org.eclipse.core.runtime.IPath;

/**
 * @author alain
 */
public interface IPathEntryContainer {

	/**
	 * Answers the set of path entries this container is mapping to.
	 * <p>
	 * The set of entries associated with a container may contain any of the following:
	 * <ul>
	 * <li> library entries (<code>CDT_LIBRARY</code>) </li>
	 * <li> project entries (<code>CDT_PROJECT</code>) </li>
	 * <li> macro entries (<code>CDT_MACRO</code>) </li>
	 * <li> include entries (<code>CDT_INCLUDE</code>) </li>
	 * </ul>
	 * A container can neither reference further containers.
	 *
	 * @return IPathEntry[] - the entries this container represents
	 * @see IPathEntry
	 */
	IPathEntry[] getPathEntries();
	
	/**
	 * Answers a readable description of this container
	 *
	 * @return String - a string description of the container
	 */
	String getDescription();

	/**
	 * Answers the container path identifying this container.
	 * A container path is formed by a first ID segment followed with extra segments, which
	 * can be used as additional hints for resolving to this container.
	 * <p>
	 * The container ID is also used to identify a<code>ClasspathContainerInitializer</code>
	 * registered on the extension point "org.eclipse.jdt.core.classpathContainerInitializer", which can
	 * be invoked if needing to resolve the container before it is explicitly set.
	 * <p>
	 * @return IPath - the container path that is associated with this container
	 */
	IPath getPath();
	
}
