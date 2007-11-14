/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Tobias Schwarz (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - [182454] improve getAbsoluteName() documentation
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Xuan Chen        (IBM)        - [160775] [api] rename (at least within a zip) blocks UI thread
 *******************************************************************************/
package org.eclipse.rse.tests.internal.testsubsystem;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.internal.ui.view.SystemPerspectiveHelpers;
import org.eclipse.rse.internal.ui.view.SystemView;
import org.eclipse.rse.tests.RSETestsPlugin;
import org.eclipse.rse.tests.testsubsystem.TestSubSystemAddAction;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystemAddTarget;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystemNode;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystemNodeContainer;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * Adapter for all nodes and container nodes.
 */
public class TestSubSystemNodeAdapter extends AbstractSystemViewAdapter
	implements ISystemRemoteElementAdapter, ITestSubSystemAddTarget {

	/**
	 * Constructor.
	 */
	public TestSubSystemNodeAdapter() {
		super();
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

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object element) {
		if (isTestSubSystemNodeContainer(element)) {
			return RSETestsPlugin.getDefault().getImageDescriptor("ICON_ID_BRANCH");  //$NON-NLS-1$
		}
		else if (isTestSubSystemNode(element)) {
			return RSETestsPlugin.getDefault().getImageDescriptor("ICON_ID_LEAF");  //$NON-NLS-1$
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (isTestSubSystemNode(element)) {
			return ((ITestSubSystemNode)element).getName();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IRemoteObjectIdentifier#getAbsoluteName(java.lang.Object)
	 */
	public String getAbsoluteName(Object element) {
		assert isTestSubSystemNode(element);
		ITestSubSystemNode node = (ITestSubSystemNode) element;
		String absName = node.getName();
		node = node.getParent();
		while (node != null) {
			absName = node.getName() + "/" + absName; //$NON-NLS-1$
			node = node.getParent();
		}
		return absName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getType(java.lang.Object)
	 */
	public String getType(Object element) {
		if (isTestSubSystemNodeContainer(element))
			return "testSubSystemContainerNode"; //$NON-NLS-1$
		else if (isTestSubSystemNode(element))
			return "testSubSystemNode"; //$NON-NLS-1$
		else
			return "unknown"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if (isTestSubSystemNode(element))
			return ((ITestSubSystemNode)element).getParent();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(IAdaptable element) {
		if (isTestSubSystemNodeContainer(element))
			return ((ITestSubSystemNodeContainer)element).hasChildNodes();
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(IAdaptable element, IProgressMonitor monitor) {
		if (isTestSubSystemNodeContainer(element))
			return ((ITestSubSystemNodeContainer)element).getChildNodes();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#internalGetPropertyDescriptors()
	 */
	protected IPropertyDescriptor[] internalGetPropertyDescriptors() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#internalGetPropertyValue(java.lang.Object)
	 */
	protected Object internalGetPropertyValue(Object key) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getAbsoluteParentName(java.lang.Object)
	 */
	public String getAbsoluteParentName(Object element) {
		if (isTestSubSystemNode(element))
			if (((ITestSubSystemNode)element).getParent() != null)
				return ((ITestSubSystemNode)element).getParent().getName();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getSubSystemConfigurationId(java.lang.Object)
	 */
	public String getSubSystemConfigurationId(Object element) {
		return "testSubSystemConfigurationId"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteTypeCategory(java.lang.Object)
	 */
	public String getRemoteTypeCategory(Object element) {
		return "testCategory"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteType(java.lang.Object)
	 */
	public String getRemoteType(Object element) {
		return "testType"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteSubType(java.lang.Object)
	 */
	public String getRemoteSubType(Object element) {
		return "testSubType"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#refreshRemoteObject(java.lang.Object, java.lang.Object)
	 */
	public boolean refreshRemoteObject(Object oldElement, Object newElement) {
		ITestSubSystemNode oldNode = (ITestSubSystemNode) oldElement;
		ITestSubSystemNode newNode = (ITestSubSystemNode) newElement;
		newNode.setName(oldNode.getName());
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteParent(org.eclipse.swt.widgets.Shell, java.lang.Object)
	 */
	public Object getRemoteParent(Object element, IProgressMonitor monitor) throws Exception {
		if (isTestSubSystemNode(element))
			return ((ITestSubSystemNode)element).getParent();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteParentNamesInUse(org.eclipse.swt.widgets.Shell, java.lang.Object)
	 */
	public String[] getRemoteParentNamesInUse(Object element, IProgressMonitor monitor) throws Exception {
		return null;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#supportsUserDefinedActions(java.lang.Object)
	 */
	public boolean supportsUserDefinedActions(Object object) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#canDelete(java.lang.Object)
	 */
	public boolean canDelete(Object element) {
		return isTestSubSystemNode(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#showDelete(java.lang.Object)
	 */
	public boolean showDelete(Object element) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#doDelete(org.eclipse.swt.widgets.Shell, java.lang.Object, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean doDelete(Shell shell, Object element, IProgressMonitor monitor) throws Exception {
		if (isTestSubSystemNode(element)) {
			ITestSubSystemNodeContainer parent = (ITestSubSystemNodeContainer)((ITestSubSystemNode)element).getParent();
			if (parent == null) {
				parent = (ITestSubSystemNodeContainer)((ITestSubSystemNode)element).getSubSystem();
			}
			if (parent != null && isTestSubSystemNodeContainer(parent))
				return parent.removeChildNode(((ITestSubSystemNode)element));
		}
		return false;
	}
	
	/*
	 * Returns true if the element is a node.
	 */
	private boolean isTestSubSystemNode(Object element) {
		return element instanceof ITestSubSystemNode;
	}

	/*
	 * Returns true if the element is a node container.
	 */
	private boolean isTestSubSystemNodeContainer(Object element) {
		return element instanceof ITestSubSystemNodeContainer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.internal.testsubsystem.actions.ITestSubSystemAddTarget#canAdd(java.lang.Object)
	 */
	public boolean canAdd(Object element) {
		return isTestSubSystemNodeContainer(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.internal.testsubsystem.actions.ITestSubSystemAddTarget#doAdd(org.eclipse.swt.widgets.Shell, java.lang.Object, java.lang.Object, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean doAdd(Shell shell, Object container, Object element, IProgressMonitor monitor) {
		boolean added = false;
		if (isTestSubSystemNodeContainer(container) && isTestSubSystemNode(element)) {
			added = ((ITestSubSystemNodeContainer)container).addChildNode(((ITestSubSystemNode)element));
			if (added) {
				SystemView view = SystemPerspectiveHelpers.findRSEView(); 
				if (view != null) {
					view.expandSelected();
					view.refresh(container);
				}
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.internal.testsubsystem.actions.ITestSubSystemAddTarget#showAdd(java.lang.Object)
	 */
	public boolean showAdd(Object element) {
		return isTestSubSystemNodeContainer(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#canRename(java.lang.Object)
	 */
	public boolean canRename(Object element) {
		return isTestSubSystemNode(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#doRename(org.eclipse.swt.widgets.Shell, java.lang.Object, java.lang.String, IProgressMonitor)
	 */
	public boolean doRename(Shell shell, Object element, String name, IProgressMonitor monitor) throws Exception {
		if (name != null && isTestSubSystemNode(element)) {
			String oldName = ((ITestSubSystemNode)element).getName();
			if (oldName == null || !oldName.equals(name)) {
				((ITestSubSystemNode)element).setName(name);
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#showRename(java.lang.Object)
	 */
	public boolean showRename(Object element) {
		return isTestSubSystemNode(element);
	}
	
	/**
	 * This is a local RSE artifact so returning false
	 * 
	 * @param element the object to check
	 * @return false since this is not remote
	 */
	public boolean isRemote(Object element) {
		return false;
	}
}
