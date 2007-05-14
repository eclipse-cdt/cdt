/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 ********************************************************************************/

package org.eclipse.rse.internal.ui.actions;

import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeListener;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.view.ISystemTree;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.widgets.Shell;

/**
 * The action allows users to collapse the selected nodes in the Remote System Explorer tree view
 */
public class SystemCollapseAction extends SystemBaseAction {
	/**
	 * Constructor
	 * @param parent the parent shell for this action
	 */
	public SystemCollapseAction(Shell parent) {
		super(SystemResources.ACTION_COLLAPSE_SELECTED_LABEL, SystemResources.ACTION_COLLAPSE_SELECTED_TOOLTIP, parent);
		allowOnMultipleSelection(true);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_EXPAND);
		setAccelerator('-');
		setHelp(RSEUIPlugin.HELPPREFIX + "actn0024"); //$NON-NLS-1$
		setAvailableOffline(true);
	}

	/**
	 * We intercept to ensure at least one selected item is collapsable
	 * @see SystemBaseAction#updateSelection(IStructuredSelection)
	 */
	public boolean updateSelection(IStructuredSelection selection) {
		boolean enable = false;
		if ((viewer != null) && (viewer instanceof ISystemTree)) {
			return ((ISystemTree) viewer).areAnySelectedItemsExpanded();
		}
		Iterator e = selection.iterator();
		ISystemViewElementAdapter adapter = null;
		while (!enable && e.hasNext()) {
			Object selectedObject = e.next();
			adapter = getViewAdapter(selectedObject);
			if (adapter != null) {
				if (adapter.hasChildren((IAdaptable)selectedObject)) enable = true;
			}
		}
		return enable;
	}

	/**
	 * This is the method called when the user selects this action.
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		//System.out.println("Inside run of SystemRefreshAction");
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		if ((viewer != null) && (viewer instanceof ISystemResourceChangeListener)) {
			sr.fireEvent((ISystemResourceChangeListener) viewer, new SystemResourceChangeEvent("dummy", ISystemResourceChangeEvents.EVENT_COLLAPSE_SELECTED, null)); //$NON-NLS-1$
		} else
			sr.fireEvent(new SystemResourceChangeEvent("dummy", ISystemResourceChangeEvents.EVENT_COLLAPSE_SELECTED, null)); //$NON-NLS-1$
	}
}