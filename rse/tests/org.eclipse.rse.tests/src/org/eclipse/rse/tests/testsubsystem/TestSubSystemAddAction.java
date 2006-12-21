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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.tests.RSETestsPlugin;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystem;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystemAddTarget;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystemNode;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystemNodeContainer;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.swt.widgets.Shell;

/**
 * Add action for container nodes.
 * This action can add branches or leafes.
 */
public class TestSubSystemAddAction extends SystemBaseAction {
	
	private boolean fAddContainer = false;

	/**
	 * Constructor.
	 * @param text The text of this action shwon in context menues.
	 * @param addContainer True if a container node should be added,
	 * 					   False if a simple node should be added.
	 * @param shell The current shell.
	 */
	public TestSubSystemAddAction(String text, boolean addContainer, Shell shell) {
		super(text, shell);
		
		fAddContainer = addContainer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return RSETestsPlugin.getDefault().getImageDescriptor(fAddContainer ? "ICON_ID_BRANCH" : "ICON_ID_LEAF");  //$NON-NLS-1$//$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.actions.SystemBaseAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public boolean updateSelection(IStructuredSelection selection) {
		return selection.size() == 1 && checkObjectType(selection.getFirstElement());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.actions.SystemBaseAction#checkObjectType(java.lang.Object)
	 */
	public boolean checkObjectType(Object selectedObject) {
		return selectedObject instanceof ITestSubSystemNodeContainer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.actions.SystemBaseAction#run()
	 */
	public void run() {
		IStructuredSelection selection = getSelection();
		Object object = selection.getFirstElement();
		ISystemRemoteElementAdapter adapter = getRemoteAdapter(object);
		if (adapter != null && adapter instanceof ITestSubSystemAddTarget && object instanceof ITestSubSystemNodeContainer) {
			ITestSubSystemAddTarget addTarget = (ITestSubSystemAddTarget)adapter;
			ITestSubSystemNodeContainer container = (ITestSubSystemNodeContainer)object;
			String name = ((object instanceof ITestSubSystem) ? "" : adapter.getName(container) + ":") + container.getChildNodeCount(); //$NON-NLS-1$ //$NON-NLS-2$
			ITestSubSystemNode node = fAddContainer ? new TestSubSystemContainerNode(name) : new TestSubSystemNode(name + ";");  //$NON-NLS-1$
			addTarget.doAdd(getShell(), container, node, null);
		}
	}
}
