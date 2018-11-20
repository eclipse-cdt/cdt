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
public interface IIncludeEntry extends IPathEntry {

	/**
	 * Returns the include path
	 * @return IPath
	 */
	IPath getIncludePath();

	/**
	 * Return the base path of the includePath
	 * @return IPath
	 */
	IPath getBasePath();

	/**
	 * Return the includePath with the base path.
	 */
	IPath getFullIncludePath();

	/**
	 * Return the reference path
	 */
	IPath getBaseReference();

	/**
	 * Whether or not it a system include path
	 * @return boolean
	 */
	boolean isSystemInclude();

	/**
	 * If isRecursive() is true, specify an exclude file patterns.
	 * @return IPath
	 */
	IPath[] getExclusionPatterns();

	/**
	 * Returns a char based representation of the exclusions patterns full path.
	 */
	public char[][] fullExclusionPatternChars();

}
