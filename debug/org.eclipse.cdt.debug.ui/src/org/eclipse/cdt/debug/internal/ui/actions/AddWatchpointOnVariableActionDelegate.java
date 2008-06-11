/*******************************************************************************
 * Copyright (c) 2007 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;


import org.eclipse.cdt.debug.internal.core.model.CVariable;
import org.eclipse.cdt.debug.internal.ui.actions.AddWatchpointDialog;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;


public class AddWatchpointOnVariableActionDelegate extends AddWatchpointActionDelegate {

	/**
	 * Constructor for Action1.
	 */
	public AddWatchpointOnVariableActionDelegate() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		IStructuredSelection selection = getSelection();
		
		if (selection == null || selection.isEmpty()) {
			return;
		}
		
		Object obj = ((TreeSelection)selection).getFirstElement();
		if (obj != null && obj instanceof CVariable) {
			CVariable var = (CVariable)obj;

			String expr = "";
			 
			try {
				expr = var.getExpressionString();
			} catch (DebugException e) {}
			 
			AddWatchpointDialog dlg = new AddWatchpointDialog(CDebugUIPlugin.getActiveWorkbenchShell(), 
					getMemorySpaceManagement()); //$NON-NLS-1$
			dlg.setExpression(expr);
			dlg.initializeRange(false, Integer.toString(var.sizeof()));
			if (dlg.open() == Window.OK) {
				addWatchpoint(dlg.getWriteAccess(), dlg.getReadAccess(), dlg.getExpression(), dlg.getMemorySpace(), dlg.getRange());
			}
		}
	}
	
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection == null || selection.isEmpty()) {
			action.setEnabled(false);
			return;
		}
		if (selection instanceof TreeSelection) {
			Object obj = ((TreeSelection)selection).getFirstElement();
			if (obj != null && obj instanceof CVariable) {
				action.setEnabled(true);
			} else {
				action.setEnabled(false);
			}
		}
	}

	private IStructuredSelection getSelection() {
		return (IStructuredSelection)getView().getViewSite().getSelectionProvider().getSelection();
	}

}
