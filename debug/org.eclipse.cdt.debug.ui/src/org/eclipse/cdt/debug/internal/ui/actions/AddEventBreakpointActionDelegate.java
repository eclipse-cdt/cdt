/*******************************************************************************
 * Copyright (c) 2008 QNX Software Systems and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * QNX Software Systems - catchpoints - bug 226689
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.internal.ui.dialogs.AddEventBreakpointDialog;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.UIMessages;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * A delegate for the "Add Event Breakpoint" action.
 */
public class AddEventBreakpointActionDelegate extends ActionDelegate implements IViewActionDelegate {

	private IViewPart fView;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	@Override
	public void init(IViewPart view) {
		setView(view);
	}

	private void setView(IViewPart view) {
		fView = view;
	}

	protected IViewPart getView() {
		return fView;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		AddEventBreakpointDialog dlg = new AddEventBreakpointDialog(CDebugUIPlugin.getActiveWorkbenchShell());
		if (dlg.isActive() == false) {
			String message = ActionMessages.getString("AddEventBreakpointActionDelegate.2"); //$NON-NLS-1$
			MessageDialog.openError( getView().getSite().getShell(), UIMessages.getString( "CDebugUIPlugin.0" ), message);  //$NON-NLS-1$
		} else {
			if (dlg.open() == Window.OK) {
				addEventBreakpoint(dlg.getEventTypeId(), dlg.getEventArgument());
			}
		}
	}

	protected void addEventBreakpoint(String id, String arg) {
		if (getResource() == null)
			return;
		try {
			CDIDebugModel.createEventBreakpoint(id, arg, true);
		} catch (CoreException ce) {
			CDebugUIPlugin.errorDialog(ActionMessages.getString("AddEventBreakpointActionDelegate.0"), ce); //$NON-NLS-1$
		}
	}

	private IResource getResource() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

}
