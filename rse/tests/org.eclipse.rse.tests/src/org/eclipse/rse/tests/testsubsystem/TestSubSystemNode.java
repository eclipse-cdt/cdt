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
package org.eclipse.rse.tests.testsubsystem;

import org.eclipse.rse.core.subsystems.AbstractResource;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystemNode;

/**
 * A simple node (leaf).
 */
public class TestSubSystemNode extends AbstractResource
	implements ITestSubSystemNode {
	
	private String fName;
	private ITestSubSystemNode fParent;

	/**
	 * Constructor.
	 * @param name The name for this node shown in the tree.
	 */
	public TestSubSystemNode(String name) {
		super();
		fName = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.testsubsystem.ITestSubSystemNode#getName()
	 */
	public String getName() {
		return fName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.testsubsystem.ITestSubSystemNode#setName(java.lang.String)
	 */
	public void setName(String name) {
		fName = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.testsubsystem.ITestSubSystemNode#setParent(org.eclipse.rse.tests.testsubsystem.ITestSubSystemNode)
	 */
	public void setParent(ITestSubSystemNode parent) {
		fParent = parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.testsubsystem.ITestSubSystemNode#getParent()
	 */
	public ITestSubSystemNode getParent() {
		return fParent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractResource#getSubSystem()
	 */
	public ISubSystem getSubSystem() {
		ISubSystem subSystem = super.getSubSystem();
		if (subSystem == null && getParent() != null) {
			subSystem = getParent().getSubSystem();
		}
		return subSystem;
	}
}
