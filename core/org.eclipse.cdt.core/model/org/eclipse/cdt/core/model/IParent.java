/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	public List getChildrenOfType(int type) throws CModelException;
	/**
	 * Returns whether this element has one or more immediate children.
	 * This is a convenience method, and may be more efficient than
	 * testing whether <code>getChildren</code> is an empty array.
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource
	 */
	boolean hasChildren();
	
}
