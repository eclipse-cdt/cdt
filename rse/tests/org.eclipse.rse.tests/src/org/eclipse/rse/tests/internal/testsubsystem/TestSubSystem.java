/* *******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Tobias Schwarz (Wind River) - initial contribution.
 * *******************************************************************************/
package org.eclipse.rse.tests.internal.testsubsystem;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.tests.testsubsystem.TestSubSystemContainerNode;
import org.eclipse.rse.tests.testsubsystem.TestSubSystemNode;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystem;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystemNode;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystemNodeContainer;

/**
 * Simple test subsystem with branches and leafes.
 * Further childs can be added or removed via context menu actions.
 */
public class TestSubSystem extends SubSystem implements ITestSubSystem {

	private ArrayList fChildren = new ArrayList();

	/**
	 * Constructor.
	 * @param host
	 * @param connectorService
	 */
	public TestSubSystem(IHost host, IConnectorService connectorService) {
		super(host, connectorService);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystem#initializeSubSystem(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void initializeSubSystem(IProgressMonitor monitor) {		
		TestSubSystemContainerNode parent0 = new TestSubSystemContainerNode("0"); //$NON-NLS-1$
		TestSubSystemContainerNode child0 = new TestSubSystemContainerNode("0:0"); //$NON-NLS-1$
		parent0.addChildNode(child0);
		parent0.addChildNode(new TestSubSystemContainerNode("0:1")); //$NON-NLS-1$
		parent0.addChildNode(new TestSubSystemContainerNode("0:2")); //$NON-NLS-1$
		parent0.addChildNode(new TestSubSystemNode("0:3;")); //$NON-NLS-1$
		parent0.addChildNode(new TestSubSystemContainerNode("0:4")); //$NON-NLS-1$
		child0.addChildNode(new TestSubSystemNode("0:0:0;")); //$NON-NLS-1$
		addChildNode(parent0);
		addChildNode(new TestSubSystemContainerNode("1")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystem#uninitializeSubSystem(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void uninitializeSubSystem(IProgressMonitor monitor) {
		fChildren.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystem#getObjectWithAbsoluteName(java.lang.String)
	 */
	public Object getObjectWithAbsoluteName(String key) {
		ITestSubSystemNode[] childs = getChildNodes();
		for (int i = 0; i < childs.length; i++) {
			if (childs[i].getName().equalsIgnoreCase(key)) {
				return childs[i];
			}
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystem#internalResolveFilterString(org.eclipse.core.runtime.IProgressMonitor, java.lang.String)
	 */
	protected Object[] internalResolveFilterString(IProgressMonitor monitor, String filterString) throws InvocationTargetException, InterruptedException {
		ArrayList filteredChilds = new ArrayList();
		ITestSubSystemNode[] childs = getChildNodes();
		for (int i = 0; i < childs.length; i++) {
			if (childs[i].getName().matches(filterString)) {
				filteredChilds.add(childs[i]);
			}
		}

		return filteredChilds.toArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystem#internalResolveFilterString(org.eclipse.core.runtime.IProgressMonitor, java.lang.Object, java.lang.String)
	 */
	protected Object[] internalResolveFilterString(IProgressMonitor monitor, Object parent, String filterString) throws InvocationTargetException, InterruptedException {
		ArrayList filteredChilds = new ArrayList();
		if (parent instanceof ITestSubSystemNodeContainer) {
			ITestSubSystemNodeContainer container = (ITestSubSystemNodeContainer)parent;
			ITestSubSystemNode[] childs = container.getChildNodes();
			for (int i = 0; i < childs.length; i++) {
				if (childs[i].getName().matches(filterString)) {
					filteredChilds.add(childs[i]);
				}
			}
		}

		return filteredChilds.toArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.testsubsystem.ITestSubSystemNodeContainer#addChildNode(org.eclipse.rse.tests.testsubsystem.ITestSubSystemNode)
	 */
	public boolean addChildNode(ITestSubSystemNode node) {
		if (node != null && !fChildren.contains(node)) {
			node.setSubSystem(this);
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
