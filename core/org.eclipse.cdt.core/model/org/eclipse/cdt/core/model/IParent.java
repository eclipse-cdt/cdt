/*******************************************************************************
 * Copyright (c) 2000, 2013 QNX Software Systems and others.
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

import java.util.List;

/**
 * Common protocol for C elements that contain other C elements.
 */
public interface IParent {
	/**
	 * Returns the immediate children of this element.
	 * The children are in no particular order.
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource
	 */
	ICElement[] getChildren() throws CModelException;

	/**
	 * returns the children of a certain type
	 */
	public List<ICElement> getChildrenOfType(int type) throws CModelException;

	/**
	 * Returns whether this element has one or more immediate children.
	 * This is a convenience method, and may be more efficient than
	 * testing whether {@link #getChildren()} returns an empty array.
	 */
	boolean hasChildren();
}
