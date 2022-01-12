/*******************************************************************************
 * Copyright (c) 2008, 2012 QNX Software Systems and others.
 *
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * QNX Software Systems - catchpoints - bug 226689
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions.breakpoints;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.internal.ui.actions.ActionMessages;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.breakpoints.IToggleBreakpointsTargetCExtension;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * A delegate for the "Add Event Breakpoint" action.
 */
public class AddEventBreakpointActionDelegate extends ActionDelegate
		implements IViewActionDelegate, IObjectActionDelegate {

	private IViewPart fView;
	private IWorkbenchPart fPart;
	private ISelection fSelection;
	private ToggleBreakpointAdapter fDefaultToggleTarget = new ToggleBreakpointAdapter();

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	@Override
	public void init(IViewPart view) {
		setView(view);
	}

	private void setView(IViewPart view) {
		fPart = fView = view;
	}

	protected IViewPart getView() {
		return fView;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		fSelection = selection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		IToggleBreakpointsTarget toggleTarget = DebugUITools.getToggleBreakpointsTargetManager()
				.getToggleBreakpointsTarget(fPart, fSelection);
		IToggleBreakpointsTargetCExtension cToggleTarget = null;
		if (toggleTarget instanceof IToggleBreakpointsTargetCExtension) {
			cToggleTarget = (IToggleBreakpointsTargetCExtension) toggleTarget;
		} else {
			cToggleTarget = fDefaultToggleTarget;
		}
		try {
			cToggleTarget.createEventBreakpointsInteractive(fPart, fSelection);
		} catch (CoreException e) {
			CDebugUIPlugin.errorDialog(ActionMessages.getString("AddEventBreakpointActionDelegate.2"), e); //$NON-NLS-1$
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

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		fPart = targetPart;
	}

}
