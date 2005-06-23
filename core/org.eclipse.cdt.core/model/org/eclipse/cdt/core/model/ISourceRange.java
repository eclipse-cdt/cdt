/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;


/**
 * A source range defines an element's source coordinates
 */
public interface ISourceRange {

	/**
	 * Returns the 0-based starting position of this element.
	 */
	public int getStartPos();

	/**
	 * Returns the number of characters of the source code for this element.
	 */
	public int getLength();
        
	/**
	 * Returns the Id starting position of this element.
	 */
	public int getIdStartPos();

	/**
	 * Returns the number of characters of the Id for this element.
	 */
	public int getIdLength();

	/**
	 * Returns the 1-based starting line of this element.
	 */
	public int getStartLine();

	/**
	 * Returns the 1-based ending line of this element.
	 */
	public int getEndLine();
}
