/**********************************************************************
 * Created on Mar 25, 2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.model;


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
     * Entry kind constant describing an entry defined using
     * a path that begins with a variable reference.
     */
	int CDT_VARIABLE = 10;

	/**
	 * Returns the kind of this path entry.
	 *
	 * @return one of:
	 * <ul>
	 * <li><code>CDT_SOURCE</code> - this entry describes a source root in
			its project
	 * <li><code>CDT_LIBRARY</code> - this entry describes a library
	 * <li><code>CDT_PROJECT</code> - this entry describes another project
	 * <li><code>CDT_INCLUDE</code> - this entry describes a include path
	 * <li><code>CDT_MACRO</code> - this entry describes a macro definition
	 * <li><code>CDT_CONTAINER</code> - this entry describes a container id
	 * <li><code>CDT_OUTPUT</code> - this entry describes output location
	 */
	int getEntryKind();
	
	/**
	 * Returns whether this entry is exported to dependent projects.
	 * Always returns <code>false</code> for source entries (kind
	 * <code>CPE_SOURCE</code>), which cannot be exported.
	 *
	 * @return <code>true</code> if exported, and <code>false</code> otherwise
	 */
	boolean isExported();

}
