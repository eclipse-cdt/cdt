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
package org.eclipse.cdt.core;

import org.eclipse.core.runtime.IPath;

public interface ICPathEntry {

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
	 * Entry kind constant describing a path entry defined using
	 * a path that begins with a variable reference.
	 */
	int CDT_VARIABLE = 4;

	/**
	 * Entry kind constant describing a path entry identifying a
	 * include path.
	 */
	int CDT_INCLUDE = 5;
	
	/**
	 * Returns the kind of this path entry.
	 *
	 * @return one of:
	 * <ul>
	 * <li><code>CDT_SOURCE</code> - this entry describes a source root in
			its project
	 * <li><code>CDT_LIBRARY</code> - this entry describes a library
	 * <li><code>CDT_PROJECT</code> - this entry describes another project
	 * <li><code>CDT_VARIABLE</code> - this entry describes a project or library
	 *  	indirectly via a variable in the first segment of the path
	 * <li><code>CDT_INCLUDE</code> - this entry describes a include path
	 */
	int getEntryKind();
	
	/**
	 * Returns the set of patterns used to exclude resources associated with
	 * this source entry.
	 * <p>
	 * Exclusion patterns allow specified portions of the resource tree rooted
	 * at this source entry's path to be filtered out. If no exclusion patterns
	 * are specified, this source entry includes all relevent files. Each path
	 * specified must be a relative path, and will be interpreted relative
	 * to this source entry's path. File patterns are case-sensitive. A file
	 * matched by one or more of these patterns is excluded from the 
	 * corresponding ICContainer.
	 * </p>
	 * <p>
	 * The pattern mechanism is similar to Ant's. Each pattern is represented as
	 * a relative path. The path segments can be regular file or folder names or simple patterns
	 * involving standard wildcard characters.
	 * </p>
	 * <p>
	 * '*' matches 0 or more characters within a segment. So
	 * <code>*.c</code> matches <code>.c</code>, <code>a.c</code>
	 * and <code>Foo.c</code>, but not <code>Foo.properties</code>
	 * (does not end with <code>.c</code>).
	 * </p>
	 * <p>
	 * '?' matches 1 character within a segment. So <code>?.c</code> 
	 * matches <code>a.c</code>, <code>A.c</code>, 
	 * but not <code>.c</code> or <code>xyz.c</code> (neither have
	 * just one character before <code>.c</code>).
	 * </p>
	 * <p>
	 * Combinations of *'s and ?'s are allowed.
	 * </p>
	 * <p>
	 * The special pattern '**' matches zero or more segments. A path 
	 * like <code>tests/</code> that ends in a trailing separator is interpreted
	 * as <code>tests/&#42;&#42;</code>, and would match all files under the 
	 * the folder named <code>tests</code>.
	 * </p>
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>
	 * <code>tests/&#42;&#42;</code> (or simply <code>tests/</code>) 
	 * matches all files under a root folder
	 * named <code>tests</code>. This includes <code>tests/Foo.c</code>
	 * and <code>tests/example/Foo.c</code>, but not 
	 * <code>com/example/tests/Foo.c</code> (not under a root folder named
	 * <code>tests</code>).
	 * </li>
	 * <li>
	 * <code>tests/&#42;</code> matches all files directly below a root 
	 * folder named <code>tests</code>. This includes <code>tests/Foo.c</code>
	 * and <code>tests/FooHelp.c</code>
	 * but not <code>tests/example/Foo.c</code> (not directly under
	 * a folder named <code>tests</code>) or 
	 * <code>example/Foo.c</code> (not under a folder named <code>tests</code>).
	 * </li>
	 * <li>
	 * <code>&#42;&#42;/tests/&#42;&#42;</code> matches all files under any
	 * folder named <code>tests</code>. This includes <code>tests/Foo.c</code>,
	 * <code>examples/tests/Foo.c</code>, and 
	 * <code>examples/tests/unit/Foo.c</code>, but not 
	 * <code>example/Foo.c</code> (not under a folder named
	 * <code>tests</code>).
	 * </li>
	 * </ul>
	 * </p>
	 * 
	 * @return the possibly empty list of resource exclusion patterns 
	 *   associated with this source entry, and <code>null</code> for other
	 *   kinds of classpath entries
	 * @since 2.1
	 */
	IPath[] getExclusionPatterns();
	
	/**
	 * Returns the path of this CPathEntry entry.
	 *
	 * The meaning of the path of a classpath entry depends on its entry kind:<ul>
	 *	<li>Source code in the current project (<code>CDT_SOURCE</code>) -  
	 *      The path associated with this entry is the absolute path to the root folder. </li>
	 *	<li>A binary library in the current project (<code>CDT_LIBRARY</code>) - the path
	 *		associated with this entry is the absolute path to the library, and 
	 *		in case it refers to an external lib, then there is no associated resource in 
	 *		the workbench.
	 *	<li>A required project (<code>CPE_PROJECT</code>) - the path of the entry denotes the
	 *		path to the corresponding project resource.</li>
	 *  <li>A variable entry (<code>CPE_VARIABLE</code>) - the first segment of the path 
	 *      is the name of a CPath variable. If this CPath variable
	 *		is bound to the path <it>P</it>, the path of the corresponding classpath entry
	 *		is computed by appending to <it>P</it> the segments of the returned
	 *		path without the variable.</li>
	 *	<li>Include path in the current project (<code>CDT_INCLUDE</code>) -  
	 *      The path associated with this entry is the absolute path to the include folder. </li>
	 * </ul>
	 *
	 * @return the path of this classpath entry
	 */
	IPath getPath();

	/**
	 * Returns the path to the source archive or folder associated with this
	 * C path entry, or <code>null</code> if this C path entry has no
	 * source attachment.
	 * <p>
	 * Only library and variable C path entries may have source attachments.
	 * For library C path entries, the result path (if present) locates a source
	 * archive or folder. This archive or folder can be located in a project of the 
	 * workspace or outside thr workspace. For variable c path entries, the 
	 * result path (if present) has an analogous form and meaning as the 
	 * variable path, namely the first segment is the name of a c path variable.
	 * </p>
	 *
	 * @return the path to the source archive or folder, or <code>null</code> if none
	 */
	IPath getSourceAttachmentPath();

	/**
	 * Returns the path within the source archive or folder where source
	 * are located. An empty path indicates that packages are located at
	 * the root of the source archive or folder. Returns a non-<code>null</code> value
	 * if and only if <code>getSourceAttachmentPath</code> returns 
	 * a non-<code>null</code> value.
	 *
	 * @return the path within the source archive or folder, or <code>null</code> if
	 *    not applicable
	 */
	IPath getSourceAttachmentRootPath();

	/**
	 * Returns the path to map the source paths with to the source achive or folder
	 * An empty path indicates that the is a one-to-one mapping of source paths to the
	 * source achive or folder path. Returns a non-<code>null</code> value
	 * if and only if <code>getSourceAttachmentPath</code> returns 
	 * a non-<code>null</code> value.
	 *
	 * @return the path mapping within the source archive or folder, or <code>null</code> if
	 *    not applicable
	 */
	IPath getSourceAttachmentPrefixMapping();
}
