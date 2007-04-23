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
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.ui.newui.ManageConfigDialog;

/**
 * Action which changes active build configuration of the current project to 
 * the given one.
 */
public class ManageConfigsAction 
implements IWorkbenchWindowPulldownDelegate2, IObjectActionDelegate {

	protected IProject project = null;

	public void selectionChanged(IAction action, ISelection selection) {
		project = null;
		if (!selection.isEmpty()) {
	    	// case for context menu
			if (selection instanceof StructuredSelection) {
				Object[] obs = ((StructuredSelection)selection).toArray();
				for (int i=0; i<obs.length; i++) {
					if (!getProject(obs[i])) break;
				}
			}
		}
		action.setEnabled(project != null);
	}
	
	protected boolean getProject(Object ob) {
		IProject prj = null;
		
		// Extract project from selection 
		if (ob instanceof ICElement) { // for C/C++ view
			prj = ((ICElement)ob).getCProject().getProject();
		} else if (ob instanceof IResource) { // for other views
			prj = ((IResource)ob).getProject();
		}
		
		if (prj != null) {
			if (!CoreModel.getDefault().isNewStyleProject(prj))
				return false;
			
			// 2 or more projects selected - cannot handle
			if (project != null && project != prj) {
				project = null;
				return false;
			}
			// only New CDT model projects can be handled
			if (isManaged(prj)) project = prj;
			return true;
		}  
		return false;
	}

	// Check for project type.
	private boolean isManaged(IProject p) {
		if (!p.isOpen()) return false;
		ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(p, false); 
		if (prjd != null) {
			ICConfigurationDescription[] c = prjd.getConfigurations();
			if (c != null && c.length > 0) return true;
		}
		return false;
	}
	
	public void run(IAction action) {
		if (project != null) 
			ManageConfigDialog.manage(project, true);
	}
	
	public void dispose() { project = null; }
	
	// doing nothing
	public void init(IWorkbenchWindow window) { }
	public Menu getMenu(Menu parent) { return null; }
	public Menu getMenu(Control parent) { return null; }
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}
}
