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

public interface IMacroFileEntry extends IPathEntry {

	/**
	 * Returns the macroFile path.
	 * @return String
	 */
	IPath getMacroFilePath();

	/**
	 * the path is completed if it relative. 
	 */
	IPath getFullMacroFilePath();

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

}
