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


/**
 * IOuputEntry
 */
public interface IOutputEntry extends IPathEntry {

	/**
	 * Returns an array of inclusion paths affecting the
	 * source folder when looking for files recursively.
	 * @return IPath
	 */
	IPath[] getExclusionPatterns();
	
	/**
	 * Returns a char based representation of the exclusions patterns full path.
	 */
	public char[][] fullExclusionPatternChars();


}
