/********************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others. All rights reserved.
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * The action allows users to collapse the entire Remote System Explorer tree view.
 */
public class SystemCollapseAllAction extends SystemBaseAction {
	/**
	 * Constructor
	 * @param parent the shell that is employing this action
	 */
	public SystemCollapseAllAction(Shell parent) {
		super(SystemResources.ACTION_COLLAPSE_ALL_LABEL, SystemResources.ACTION_COLLAPSE_ALL_TOOLTIP, PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_ELCL_COLLAPSEALL), // D54577
				parent);
		//setHoverImageDescriptor(RSEUIPlugin.getDefault().getImageDescriptorFromIDE("elcl16/collapseall.gif")); //$NON-NLS-1$
		setHoverImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_COLLAPSEALL));
		allowOnMultipleSelection(true);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_EXPAND); // should never be used
		setSelectionSensitive(false);

		setHelp(RSEUIPlugin.HELPPREFIX + "actn0023"); //$NON-NLS-1$
		setAccelerator(SWT.CTRL | '-');
	}

	/**
	 * @see SystemBaseAction#updateSelection(IStructuredSelection)
	 */
	public boolean updateSelection(IStructuredSelection selection) {
		return true;
	}

	/**
	 * This is the method called when the user selects this action.
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		if ((viewer != null) && (viewer instanceof ISystemResourceChangeListener)) {
			sr.fireEvent((ISystemResourceChangeListener) viewer, new SystemResourceChangeEvent("false", ISystemResourceChangeEvents.EVENT_COLLAPSE_ALL, null)); //$NON-NLS-1$
		} else
			sr.fireEvent(new SystemResourceChangeEvent("false", ISystemResourceChangeEvents.EVENT_COLLAPSE_ALL, null)); //$NON-NLS-1$
	}
}