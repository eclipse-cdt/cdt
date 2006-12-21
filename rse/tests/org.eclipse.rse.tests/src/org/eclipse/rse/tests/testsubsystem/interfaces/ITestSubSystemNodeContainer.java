/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Tobias Schwarz (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.tests.testsubsystem.interfaces;

/**
 * Interface for node container.
 */
public interface ITestSubSystemNodeContainer {

	/**
	 * Adds the node to the list of childs if not already in the list.
	 * @param node The node that should be added to the list of childs.
	 * @return True if the node was added.
	 */
	public boolean addChildNode(ITestSubSystemNode node);

	/**
	 * Removes the node from the list of childs.
	 * If the node is a node container, all children are removed recursively.
	 * @param node The node that should be removed from the list of childs.
	 * @return True if the node exists as a child and was removed.
	 */
	public boolean removeChildNode(ITestSubSystemNode node);

	/**
	 * Removes all children of this container.
	 * If a hild node is a node container, all children are removed recursively.
	 * @return True if children were removed.
	 */
	public boolean removeAllChildNodes();

	/**
	 * Returns true if this container has children.
	 * @return True if this node has children.
	 */
	public boolean hasChildNodes();
	
	/**
	 * Returns the number of children.
	 * @return The number of children.
	 */
	public int getChildNodeCount();

	/**
	 * Returns an array of all children.
	 * @return Array of children.
	 */
	public ITestSubSystemNode[] getChildNodes();
}
