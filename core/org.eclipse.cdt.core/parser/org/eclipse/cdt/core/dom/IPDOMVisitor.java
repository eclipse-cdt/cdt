/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom;

import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
public interface IPDOMVisitor {
	/**
	 * Walks the nodes in a PDOM. Returns true to visit the children of
	 * the node, or false to skip to the next sibling of this node.
	 * Throw CoreException to stop the visit.
	 *  
	 * @param node being visited
	 * @return whether to visit children
	 */
	public boolean visit(IPDOMNode node) throws CoreException;

	/**
	 * All children have been visited, about to go back to the parent.
	 * 
	 * @param node that has just completed visitation
	 * @throws CoreException
	 */
	public void leave(IPDOMNode node) throws CoreException;
}
