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

public interface IMacroEntry extends IPathEntry {

	/**
	 * Returns the macro name.
	 * @return String
	 */
	String getMacroName();

	/**
	 * Returns the macro value.
	 * @return String
	 */
	String getMacroValue();

	/**
	 * Returns an array of inclusion paths affecting the
	 * resource when looking for files recursively.
	 * @return IPath
	 */
	IPath[] getExclusionPatterns();

	/**
	 * Returns a char based representation of the exclusions patterns full path.
	 */
	public char[][] fullExclusionPatternChars();

	/**
	 * Return the base path.
	 * @return
	 */
	IPath getBasePath();

	/**
	 * return the base reference
	 * IMacroEntry
	 */
	IPath getBaseReference();

}
