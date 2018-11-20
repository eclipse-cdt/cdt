/*******************************************************************************
 * Copyright (c) 2000, 2014 QNX Software Systems and others.
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

/**
 * A source range defines an element's source coordinates
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ISourceRange {
	/**
	 * Returns the zero-based starting position of this element.
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
