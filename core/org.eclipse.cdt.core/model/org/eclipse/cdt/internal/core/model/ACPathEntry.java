/**********************************************************************
 * Created on 25-Mar-2003
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
package org.eclipse.cdt.internal.core.model;

import org.eclipse.core.runtime.IPath;

public abstract class ACPathEntry extends CPathEntry {

	public static IPath[] NO_EXCLUSION_PATTERNS = {};
	IPath[] exclusionPatterns;
	boolean isRecursive;

	public ACPathEntry (int kind, boolean isRecursive, IPath[] exclusionPatterns, boolean isExported) {
		super(kind, isExported);
		this.exclusionPatterns = exclusionPatterns;
		this.isRecursive = isRecursive;
	}

	/**
	 * Returns the exclusion patterns
	 * @return IPath[]
	 */
	public IPath[] getExclusionPatterns() {
		return exclusionPatterns;
	}
 
	/**
	 * Whether or not it is recursive
	 * @return boolean
	 */
	public boolean isRecursive() {
		return isRecursive;
	}
 
}
