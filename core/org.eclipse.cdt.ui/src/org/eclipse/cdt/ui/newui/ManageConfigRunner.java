/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.window.Window;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.ui.CUIPlugin;

public class ManageConfigRunner implements IConfigManager {
	private static final String MANAGE_TITLE = UIMessages.getString("ManageConfigDialog.0");  //$NON-NLS-1$

	protected static ManageConfigRunner instance = null;
	protected IProject project = null;
	
	public static ManageConfigRunner getDefault() {
		if (instance == null)
			instance = new ManageConfigRunner();
		return instance;
	}
	
	public boolean canManage(Object[] obs) {
		project = null;
		for (int i=0; i<obs.length; i++)
			if (!getProject(obs[i])) 
				break;
		return project != null;
	}

	public boolean manage(Object[] obs, boolean doOk) {
		if (!canManage(obs))
			return false;
		
		ManageConfigDialog d = new ManageConfigDialog(CUIPlugin.getActiveWorkbenchShell(),
				project.getName()+ " : " + MANAGE_TITLE, project); //$NON-NLS-1$
		boolean result = false;
		if (d.open() == Window.OK) {
			if (doOk) {
				CDTPropertyManager.performOk(d.getShell());
			}
			AbstractPage.updateViews(project);
			result = true;
		}
		return result;
	}

	private boolean getProject(Object ob) {
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
}
