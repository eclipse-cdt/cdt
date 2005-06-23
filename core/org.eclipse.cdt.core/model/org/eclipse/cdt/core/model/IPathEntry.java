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
	int CDT_PROJECT = 1 << 2;

	/**
	 * Entry kind constant describing a path entry identifying a
	 * folder containing source code to be compiled.
	 */
	int CDT_SOURCE = 1 << 3;

	/**
	 * Entry kind constant describing a path entry identifying a
	 * include path.
	 */
	int CDT_INCLUDE = 1 << 4;
	
	/**
	 * Entry kind constant describing a path entry representing
	 * a container id.
	 *
	 */
	int CDT_CONTAINER = 1 << 5;

	/**
	 * Entry kind constant describing a path entry representing
	 * a macro definition.
	 *
	 */
	int CDT_MACRO = 1 << 6;

	/**
	 * Entry kind constant describing output location
	 *
	 */
	int CDT_OUTPUT = 1 << 7;

	/**
	 * Entry kind constant describing a path entry representing
	 * a file that will be process file as if "#include "file"" appeared as
	 * the first line of the source file.
	 */
	int CDT_INCLUDE_FILE = 1 << 8;

	/**
	 * Entry kind constant describing a path entry representing
	 * a file that will be process file as if "#include "file"" appeared as
	 * the first line of the source file but only the macro definitions are kept.
	 * 
	 */
	int CDT_MACRO_FILE = 1 << 9;

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
	 * <li><code>CDT_INCLUDE_FILE</code> - this entry describes a file to be process as an include
	 * <li><code>CDT_MACRO_FILE</code> - this entry describes a file containing macro definitions
	 */
	int getEntryKind();

	/**
	 * 
	 * @return true if exported.
	 */
	boolean isExported();

	/**
	 * Returns the affected IPath
	 * 
	 * @return IPath
	 */
	IPath getPath();

}
