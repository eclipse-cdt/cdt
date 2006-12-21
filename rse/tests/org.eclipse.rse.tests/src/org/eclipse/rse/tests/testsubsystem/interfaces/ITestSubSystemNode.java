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

import org.eclipse.rse.core.subsystems.ISubSystem;

/**
 * Interface for all test subsystem nodes.
 */
public interface ITestSubSystemNode {

	/**
	 * Returns the name of this node shown in the tree.
	 * @return The name of the node.
	 */
	public String getName();

	/**
	 * Set the name for this node.
	 * @param name The Name of this node.
	 */
	public void setName(String name);

	/**
	 * Set the subsystem this node belongs to.
	 * This value should be set automatically when adding this node to a subsystem or other node.
	 * @param subSystem The subsystem.
	 */
	public void setSubSystem(ISubSystem subSystem);

	/**
	 * Returns the subsystem this node belongs to.
	 * @return The subsystem.
	 */
	public ISubSystem getSubSystem();

	/**
	 * Set the node container this node belongs to.
	 * This value should be set automatically when adding this node to a node container.
	 * @param parent The parent node container.
	 */
	public void setParent(ITestSubSystemNode parent);

	/**
	 * Returns the parent node this node belongs to.
	 * @return The parent node.
	 */
	public ITestSubSystemNode getParent();
}
