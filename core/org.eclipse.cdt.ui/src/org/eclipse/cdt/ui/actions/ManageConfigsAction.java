/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;

import org.eclipse.cdt.ui.newui.IConfigManager;
import org.eclipse.cdt.ui.newui.ManageConfigSelector;

/**
 * Action which changes active build configuration of the current project to 
 * the given one.
 */
public class ManageConfigsAction 
implements IWorkbenchWindowPulldownDelegate2, IObjectActionDelegate {
	IProject[] obs = null; 
	
	public void selectionChanged(IAction action, ISelection selection) {
		if (!selection.isEmpty()) {
	    	// case for context menu
			if (selection instanceof StructuredSelection) {
				obs = ManageConfigSelector.getProjects(((StructuredSelection)selection).toArray());
				action.setEnabled(ManageConfigSelector.getManager(obs) != null);
				return;
			}
		}
		action.setEnabled(false);
	}
	
	public void run(IAction action) {
		IConfigManager cm = ManageConfigSelector.getManager(obs);
		if (cm != null && obs != null)
			cm.manage(obs, true);
	}
	
	public void dispose() { obs = null; }
	
	// doing nothing
	public void init(IWorkbenchWindow window) { }
	public Menu getMenu(Menu parent) { return null; }
	public Menu getMenu(Control parent) { return null; }
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}
}
