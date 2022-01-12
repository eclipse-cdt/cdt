/*******************************************************************************
 * Copyright (c) 2008 ARM Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * ARM Limited - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.disassembly;

/**
 * A content update request for a source viewer.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 *
 * This interface is experimental.
 */
public interface IDocumentElementContentUpdate extends IDocumentUpdate {

	/**
	 * Returns the line number associated with the base element.
	 * Can be outside of the requested line interval.
	 *
	 * @return line number associated with the element
	 */
	public int getOriginalOffset();

	/**
	 * Returns the number of lines requested.
	 *
	 * @return number of lines requested
	 */
	public int getRequestedLineCount();

	/**
	 * Sets the offset of the base element
	 *
	 * @param offset offset of the base element
	 */
	public void setOffset(int offset);

	/**
	 * Sets the number of lines in this update request
	 *
	 * @param lineCount number of lines
	 */
	public void setLineCount(int lineCount);

	/**
	 * Adds a source element for the given line number
	 *
	 * @param line line number
	 * @param element element to add
	 */
	public void addElement(int line, Object element) throws IndexOutOfBoundsException;

	/**
	 * Indicates whether or not the element should be revealed
	 *
	 * @return whether or not the element should be revealed
	 */
	public boolean reveal();
}
