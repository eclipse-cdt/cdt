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

import java.util.ArrayList;

import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystemNode;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystemNodeContainer;


/**
 * A simple container node (branch).
 */
public class TestSubSystemContainerNode extends TestSubSystemNode
	implements ITestSubSystemNode, ITestSubSystemNodeContainer {
	
	private ArrayList fChildren = new ArrayList();

	/**
	 * Constructor.
	 * @param name The name of the conatiner node shown in the tree.
	 */
	public TestSubSystemContainerNode(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.testsubsystem.ITestSubSystemNodeContainer#addChildNode(org.eclipse.rse.tests.testsubsystem.ITestSubSystemNode)
	 */
	public boolean addChildNode(ITestSubSystemNode node) {
		if (node != null && !fChildren.contains(node)) {
			node.setSubSystem(getSubSystem());
			node.setParent(this);
			fChildren.add(node);
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.testsubsystem.ITestSubSystemNodeContainer#removeChildNode(org.eclipse.rse.tests.testsubsystem.ITestSubSystemNode)
	 */
	public boolean removeChildNode(ITestSubSystemNode node) {
		if (node != null && fChildren.contains(node)) {
			if (node instanceof ITestSubSystemNodeContainer) {
				((ITestSubSystemNodeContainer)node).removeAllChildNodes();
			}
			fChildren.remove(node);
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.testsubsystem.ITestSubSystemNodeContainer#removeAllChildNodes()
	 */
	public boolean removeAllChildNodes() {
		if (!fChildren.isEmpty()) {
			ITestSubSystemNode[] childs = getChildNodes();
			for (int i = 0; i < childs.length; i++) {
				if (childs[i] instanceof ITestSubSystemNodeContainer) {
					((ITestSubSystemNodeContainer)childs[i]).removeAllChildNodes();
				}
			}
			fChildren.clear();
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.testsubsystem.ITestSubSystemNodeContainer#hasChildNodes()
	 */
	public boolean hasChildNodes() {
		return !fChildren.isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.testsubsystem.ITestSubSystemNodeContainer#getChildNodeCount()
	 */
	public int getChildNodeCount() {
		return fChildren.size();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.testsubsystem.ITestSubSystemNodeContainer#getChildNodes()
	 */
	public ITestSubSystemNode[] getChildNodes() {
		return (ITestSubSystemNode[])fChildren.toArray(new ITestSubSystemNode[fChildren.size()]);
	}
}
