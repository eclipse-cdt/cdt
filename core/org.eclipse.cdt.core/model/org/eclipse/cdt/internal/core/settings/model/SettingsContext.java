/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
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
	private static final int USER_FLAGS_MASK = 0x0000ffff;
	public static final int CFG_DATA_CACHED = 1 << 15;
	
	private IProjectDescription fEDes;
	private IProject fProject;
	private CompositeWorkspaceRunnable fRunnable;
	private int fFlags;
	
	
	public SettingsContext(IProject project){
		fProject = project;
	}

	public IProject getProject(){
		return fProject;
	}
	
	void init(CConfigurationDescriptionCache cfg){
		int flags = 0;
		if(cfg.getBaseCache() != null)
			flags |= CFG_DATA_CACHED;
		fFlags = flags;
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
	
	public IWorkspaceRunnable createOperationRunnable() {
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

	public int getAllConfigurationSettingsFlags() {
		return fFlags;
	}

	public void setConfigurationSettingsFlags(int flags) {
		//system flags are read only; 
		flags &= USER_FLAGS_MASK;
		fFlags |= flags;
	}

	public boolean isBaseDataCached() {
		return (fFlags & CFG_DATA_CACHED) != 0;
	}
}
