/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.newui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.ui.CUIPlugin;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ManageConfigRunner implements IConfigManager {
	private static final String MANAGE_TITLE = UIMessages.getString("ManageConfigDialog.0");  //$NON-NLS-1$

	protected static ManageConfigRunner instance = null;
	
	private ICProjectDescription des = null;
	private IProject prj = null;
	
	public static ManageConfigRunner getDefault() {
		if (instance == null)
			instance = new ManageConfigRunner();
		return instance;
	}
	
	public boolean canManage(IProject[] obs) {
		// Only one project can be accepted
		return (obs != null && obs.length == 1);
	}

	public boolean manage(IProject[] obs, boolean doOk) {
		if (!canManage(obs))
			return false;
		
		ManageConfigDialog d = new ManageConfigDialog(CUIPlugin.getActiveWorkbenchShell(),
				obs[0].getName()+ ": " + MANAGE_TITLE, obs[0]); //$NON-NLS-1$
		boolean result = false;
		if (d.open() == Window.OK) {
			if (doOk) {
				des = d.getProjectDescription();
				prj = obs[0];
				if(des != null) 
					try {
						PlatformUI.getWorkbench().getProgressService().run(false, false, getRunnable());
					} catch (InvocationTargetException e) {}
					  catch (InterruptedException e) {}
			}
			AbstractPage.updateViews(obs[0]);
			result = true;
		} else if (doOk) {
			CDTPropertyManager.performCancel(d.getShell());
		}
		return result;
	}
	
	public IRunnableWithProgress getRunnable() {
		return new WorkspaceModifyDelegatingOperation(new IRunnableWithProgress() {
			public void run(IProgressMonitor imonitor) throws InvocationTargetException, InterruptedException {
				CUIPlugin.getDefault().getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						try {
							CoreModel.getDefault().setProjectDescription(prj, des);
						} catch (CoreException e) {
							e.printStackTrace();
						}
					}
				});
			}
		});
	}
}
