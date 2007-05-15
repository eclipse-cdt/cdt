/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Tobias Schwarz (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 *******************************************************************************/
package org.eclipse.rse.tests.internal.testsubsystem;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.internal.ui.view.SystemPerspectiveHelpers;
import org.eclipse.rse.internal.ui.view.SystemView;
import org.eclipse.rse.internal.ui.view.SystemViewSubSystemAdapter;
import org.eclipse.rse.tests.testsubsystem.TestSubSystemAddAction;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystemAddTarget;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystemNode;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystemNodeContainer;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.swt.widgets.Shell;

/**
 * Adapter for subsystem node.
 */
public class TestSubSystemAdapter extends SystemViewSubSystemAdapter
	implements ISystemRemoteElementAdapter, ITestSubSystemAddTarget {

	/**
	 * Constructor.
	 */
	public TestSubSystemAdapter() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.internal.testsubsystem.actions.ITestSubSystemAddTarget#canAdd(java.lang.Object)
	 */
	public boolean canAdd(Object element) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.internal.testsubsystem.actions.ITestSubSystemAddTarget#doAdd(org.eclipse.swt.widgets.Shell, java.lang.Object, java.lang.Object, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean doAdd(Shell shell, Object container, Object element, IProgressMonitor monitor) {
		boolean added = false;
		if (container instanceof ITestSubSystemNodeContainer && element instanceof ITestSubSystemNode) {
			added = ((ITestSubSystemNodeContainer)container).addChildNode(((ITestSubSystemNode)element));
			if (added) {
				SystemView view = SystemPerspectiveHelpers.findRSEView();
				if (view != null) {
					view.expandSelected();
					view.refresh(container, true);
				}
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.internal.testsubsystem.actions.ITestSubSystemAddTarget#showAdd(java.lang.Object)
	 */
	public boolean showAdd(Object element) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getAbsoluteParentName(java.lang.Object)
	 */
	public String getAbsoluteParentName(Object element) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteParent(org.eclipse.swt.widgets.Shell, java.lang.Object)
	 */
	public Object getRemoteParent(Object element, IProgressMonitor monitor) throws Exception {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteParentNamesInUse(org.eclipse.swt.widgets.Shell, java.lang.Object)
	 */
	public String[] getRemoteParentNamesInUse(Object element, IProgressMonitor monitor) throws Exception {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteSubType(java.lang.Object)
	 */
	public String getRemoteSubType(Object element) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteType(java.lang.Object)
	 */
	public String getRemoteType(Object element) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteTypeCategory(java.lang.Object)
	 */
	public String getRemoteTypeCategory(Object element) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getSubSystemConfigurationId(java.lang.Object)
	 */
	public String getSubSystemConfigurationId(Object element) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#refreshRemoteObject(java.lang.Object, java.lang.Object)
	 */
	public boolean refreshRemoteObject(Object oldElement, Object newElement) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#supportsUserDefinedActions(java.lang.Object)
	 */
	public boolean supportsUserDefinedActions(Object object) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#addActions(org.eclipse.rse.ui.SystemMenuManager, org.eclipse.jface.viewers.IStructuredSelection, org.eclipse.swt.widgets.Shell, java.lang.String)
	 */
	public void addActions(SystemMenuManager menu, IStructuredSelection selection, Shell parent, String menuGroup) {
		if (selection.size() == 1 && isTestSubSystemNodeContainer(selection.getFirstElement())) {
			if (canAdd(selection.getFirstElement())) {
				menu.add(menuGroup, new TestSubSystemAddAction("Add branch", true, getShell())); //$NON-NLS-1$
				menu.add(menuGroup, new TestSubSystemAddAction("Add leaf", false, getShell())); //$NON-NLS-1$
			}
		}
	}

	/*
	 * Returns true if the element is a node container.
	 */
	private boolean isTestSubSystemNodeContainer(Object element) {
		return element instanceof ITestSubSystemNodeContainer;
	}
}
