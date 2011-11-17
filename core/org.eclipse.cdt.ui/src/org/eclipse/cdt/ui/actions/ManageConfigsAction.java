/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
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
 * Action which lets to manage (add/remove etc.) build configurations of the project.
 */
public class ManageConfigsAction
implements IWorkbenchWindowPulldownDelegate2, IObjectActionDelegate {
	IProject[] obs = null;

	@Override
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

	@Override
	public void run(IAction action) {
		IConfigManager cm = ManageConfigSelector.getManager(obs);
		if (cm != null && obs != null)
			cm.manage(obs, true);
	}

	@Override
	public void dispose() { obs = null; }

	// doing nothing
	@Override
	public void init(IWorkbenchWindow window) { }
	@Override
	public Menu getMenu(Menu parent) { return null; }
	@Override
	public Menu getMenu(Control parent) { return null; }
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}
}
