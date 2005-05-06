/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.core.search;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * Interface used for returning matches from the Search Engine
 *
 */
public interface IMatch {
	
	int getElementType();

	int getVisibility();

	String getName();

	String getParentName();

	IResource getResource();
	
	IPath getLocation();

	IPath getReferenceLocation();
	/**
	 * Returns the start offset for this match. Note that clients should use
	 * getOffsetType to determine if this is a LINE or an OFFSET
	 * @return start offset
	 */
	int getStartOffset();
	/**
	 * Returns the end offset for this match. Note that clients should use
	 * getOffsetType to determine if this is a LINE or an OFFSET. The end offset
	 * is meaningless for LINE offsets; instead use IDocument.getLineLength to
	 * figure out the length of the line.
	 * @return end offset
	 */
	int getEndOffset();
	/**
	 * Returns the type of offset either IIndex.LINE or IIndex.OFFSET
	 * @return IIndex.LINE or IIndex.OFFSET
	 */
	public int getOffsetType();

	boolean isStatic();
	boolean isConst();
	boolean isVolatile();
}
