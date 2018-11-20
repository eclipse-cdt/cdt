/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.qt.core.location;

/**
 * Represents a location in a source file. Uses the {@link IPosition} interface to store the start and end locations as a
 * line/offset pair.
 */
public interface ISourceLocation {
	/**
	 * Gets the String representing the source of this <code>ISourceLocation</code>
	 *
	 * @return the source or <code>null</code> if not available
	 */
	public String getSource();

	/**
	 * Gets the zero-indexed offset indicating the start of this <code>ISourceLocation</code>
	 *
	 * @return the start offset
	 */
	public IPosition getStart();

	/**
	 * Gets the zero-indexed offset indicating the end of this <code>ISourceLocation</code>
	 *
	 * @return the end offset
	 */
	public IPosition getEnd();
}
