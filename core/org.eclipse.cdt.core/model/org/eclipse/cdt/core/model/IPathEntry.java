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


public interface IPathEntry {

	/**
	 * Entry kind constant describing a path entry identifying a
	 * library. A library is an archive containing 
	 * consisting of pre-compiled binaries.
	 */
	int CDT_LIBRARY = 1;

	/**
	 * Entry kind constant describing a path entry identifying a
	 * required project.
	 */
	int CDT_PROJECT = 2;

	/**
	 * Entry kind constant describing a path entry identifying a
	 * folder containing source code to be compiled.
	 */
	int CDT_SOURCE = 3;

	/**
	 * Entry kind constant describing a path entry identifying a
	 * include path.
	 */
	int CDT_INCLUDE = 4;
	
	/**
	 * Entry kind constant describing a path entry representing
	 * a container id.
	 *
	 */
	int CDT_CONTAINER = 5;

	/**
	 * Entry kind constant describing a path entry representing
	 * a macro definition.
	 *
	 */
	int CDT_MACRO = 6;

	/**
	 * Entry kind constant describing output location
	 *
	 */
	int CDT_OUTPUT = 7;

	/**
	 * Returns the kind of this path entry.
	 *
	 * @return one of:
	 * <ul>
	 * <li><code>CDT_SOURCE</code> - this entry describes a source root in its project
	 * <li><code>CDT_LIBRARY</code> - this entry describes a library
	 * <li><code>CDT_PROJECT</code> - this entry describes another project
	 * <li><code>CDT_INCLUDE</code> - this entry describes a include path
	 * <li><code>CDT_MACRO</code> - this entry describes a macro definition
	 * <li><code>CDT_CONTAINER</code> - this entry describes a container id
	 * <li><code>CDT_OUTPUT</code> - this entry describes output location
	 */
	int getEntryKind();
	
	/**
	 * Returns the affected IPath
	 *
	 * @return IPath
	 */
	boolean isExported();

	/**
	 * 
	 * @return IPath
	 */
	IPath getPath();

}
