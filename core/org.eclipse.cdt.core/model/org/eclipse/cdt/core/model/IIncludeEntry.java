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

import org.eclipse.core.runtime.IPath;

public interface IIncludeEntry extends IPathEntry {

	/**
	 * Returns the include path 
	 * @return IPath
	 */
	IPath getIncludePath();

	/**
	 * Whether or not it a system include path
	 * @return boolean
	 */
	boolean isSystemInclude();

	/**
	 * Whether or not the include affects the resource(if it is a folder)
	 * recursively
	 * @return boolean
	 */
	boolean isRecursive();

	/**
	 * If isRecursive() is true, specify an exclude file patterns.
	 * @return IPath
	 */
	IPath[] getExclusionPatterns();

}
