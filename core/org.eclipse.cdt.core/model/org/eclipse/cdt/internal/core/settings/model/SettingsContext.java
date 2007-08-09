/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.settings.model.IModificationContext;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager.CompositeWorkspaceRunnable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public final class SettingsContext implements IModificationContext{
	private IProjectDescription fEDes;
	private IProject fProject;
	private CompositeWorkspaceRunnable fRunnable;
	private boolean fCfgDataModifyState;
	private boolean fCfgDataCacheState;
	
	SettingsContext(IProject project){
		fProject = project;
	}

	public IProject getProject(){
		return fProject;
	}
	
	void init(CConfigurationDescriptionCache cfg){
		fCfgDataModifyState = true;
		fCfgDataCacheState = cfg.getBaseCache() != null;
	}

	IProjectDescription getEclipseProjectDescription(boolean create) throws CoreException{
		IProjectDescription eDes = fEDes;
		if(eDes == null && create){
			if(fProject == null)
				throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("SettingsContext.0")); //$NON-NLS-1$
			
			eDes = fProject.getDescription();
		}
		return eDes;
	}
	
	public IProjectDescription getEclipseProjectDescription() throws CoreException{
		return getEclipseProjectDescription(true);
	}
	
	public void setEclipseProjectDescription(IProjectDescription des)
			throws CoreException {
		if(fEDes == null)
			fEDes = des;
		else if(fEDes != des)
			throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("SettingsContext.1")); //$NON-NLS-1$
	}

	CompositeWorkspaceRunnable getCompositeWorkspaceRunnable(boolean create){
		if(fRunnable == null && create)
			fRunnable = new CompositeWorkspaceRunnable(null);
		return fRunnable;
	}
	
	public void addWorkspaceRunnable(IWorkspaceRunnable runnable){
		getCompositeWorkspaceRunnable(true).add(runnable);
	}
	
	IWorkspaceRunnable createOperationRunnable() {
		CompositeWorkspaceRunnable result = new CompositeWorkspaceRunnable(null);
		
		IWorkspaceRunnable r = getSetEclipseProjectDescriptionRunnable();
		if(r != null)
			result.add(r);
		r = getCompositeWorkspaceRunnable(false);
		if(r != null)
			result.add(r);
		
		return result.isEmpty() ? null : result;
	}
	
	private IWorkspaceRunnable getSetEclipseProjectDescriptionRunnable(){
		if(fEDes != null){
			return new IWorkspaceRunnable(){

				public void run(IProgressMonitor monitor)
						throws CoreException {
					fProject.setDescription(fEDes, monitor);
				}
			};
		}
		return null;
	}

	public boolean getBaseConfigurationDataCacheState() {
		return fCfgDataCacheState;
	}

	public void setConfiguratoinDataModifiedState(boolean modified) {
		fCfgDataModifyState = modified;
	}
	
	public boolean getConfiguratoinDataModifiedState(){
		return fCfgDataModifyState;
	}
}
