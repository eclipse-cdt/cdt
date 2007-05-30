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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.settings.model.util.ResourceChangeHandlerBase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

public class ResourceChangeHandler extends ResourceChangeHandlerBase implements  ISaveParticipant {
	CProjectDescriptionManager fMngr = CProjectDescriptionManager.getInstance();
	
	private class RcMoveHandler implements IResourceMoveHandler {
		Map fProjDesMap = new HashMap();
		Set fRemovedProjSet = new HashSet();

		public void handleProjectClose(IProject project) {
			fMngr.setLoaddedDescription(project, null, true);
		}
	
		public boolean handleResourceMove(IResource fromRc, IResource toRc) {
			boolean proceed = true;
			IProject fromProject = fromRc.getProject();
			IProject toProject = toRc.getProject();
			switch(toRc.getType()){
			case IResource.PROJECT:{
				ICProjectDescription des = getProjectDescription(fromProject, false);
				fRemovedProjSet.add(fromProject);
				if(des != null){
					((CProjectDescription)des).updateProject(toProject);
					synchronized (fMngr) {
						fMngr.setLoaddedDescription(fromProject, null, true);
						fMngr.setLoaddedDescription(toProject, des, true);
					}
					fProjDesMap.put(toProject, des);
					ICConfigurationDescription[] cfgs = des.getConfigurations();
					for(int i = 0; i < cfgs.length; i++){
						cfgs[i].getConfigurationData();
					}
				}
			}
				break;
			case IResource.FOLDER:
			case IResource.FILE:{
				IPath fromRcProjPath = fromRc.getProjectRelativePath();
				IPath toRcProjPath = toRc.getProjectRelativePath();
				if(toRcProjPath.equals(fromRcProjPath))
					break;

				if(!toProject.equals(fromProject))
					break;
				
				ICProjectDescription des = getProjectDescription(toProject, true);
				if(des != null){
					ICConfigurationDescription cfgDess[] = des.getConfigurations();
					for(int i = 0; i < cfgDess.length; i++){
						ICResourceDescription rcDescription = cfgDess[i].getResourceDescription(fromRcProjPath, true);
						if(rcDescription != null){
							try {
								rcDescription.setPath(toRcProjPath);
							} catch (WriteAccessException e) {
//							} catch (CoreException e) {
							}
						}
					}
				}
				break;
			}
			}
			
			return proceed;
		}
		
		private ICProjectDescription getProjectDescription(IResource rc, boolean load){
			IProject project = rc.getProject(); 
			ICProjectDescription des = (ICProjectDescription)fProjDesMap.get(project);
			if(des == null){
				int flags = load ? 0 : CProjectDescriptionManager.GET_IF_LOADDED;
				flags |= CProjectDescriptionManager.INTERNAL_GET_IGNORE_CLOSE;
				flags |= CProjectDescriptionManager.GET_WRITABLE;
				des = fMngr.getProjectDescription(project, flags);
				fProjDesMap.put(project, des);
			}
			return des;
		}
	
		public boolean handleResourceRemove(IResource rc) {
			boolean proceed = true;
			IProject project = rc.getProject();
			switch(rc.getType()){
			case IResource.PROJECT:
				fMngr.setLoaddedDescription(project, null, true);
				fRemovedProjSet.add(project);
				proceed = false;
				break;
			case IResource.FOLDER:
			case IResource.FILE:
				if(project.exists()){
					ICProjectDescription des = getProjectDescription(project, true);
					if(des != null){
						IPath rcProjPath = rc.getProjectRelativePath();
						ICConfigurationDescription cfgDess[] = des.getConfigurations();
						for(int i = 0; i < cfgDess.length; i++){
							ICResourceDescription rcDescription = cfgDess[i].getResourceDescription(rcProjPath, true);
							if(rcDescription != null){
								try {
									cfgDess[i].removeResourceDescription(rcDescription);
								} catch (WriteAccessException e) {
								} catch (CoreException e) {
								}
							}
						}
					}
				}
				break;
			}
			
			return proceed;
		}

		public void done() {
			for(Iterator iter = fProjDesMap.entrySet().iterator(); iter.hasNext();){
				Map.Entry entry = (Map.Entry)iter.next();
				if(fRemovedProjSet.contains(entry.getKey())){
					iter.remove();
				} else {
					ICProjectDescription des = (ICProjectDescription)entry.getValue();
					if(des == null || !des.isModified())
						iter.remove();
				}
			}
			
			if(fProjDesMap.size() != 0){
				fMngr.runWspModification(new IWorkspaceRunnable(){

					public void run(IProgressMonitor monitor) throws CoreException {
						for(Iterator iter = fProjDesMap.entrySet().iterator(); iter.hasNext();){
							Map.Entry entry = (Map.Entry)iter.next();
							IProject project = (IProject)entry.getKey();
							if(!project.isOpen())
								continue;
							
							ICProjectDescription des = (ICProjectDescription)entry.getValue();
							
							try {
								fMngr.setProjectDescription(project, des);
							} catch (CoreException e) {
								CCorePlugin.log(e);
							}
						}
					}
					
				}, new NullProgressMonitor());
			}
		}
	}
	

	protected IResourceMoveHandler createResourceMoveHandler(
			IResourceChangeEvent event) {
		return new RcMoveHandler();
	}

	/*
	 *  I S a v e P a r t i c i p a n t 
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.ISaveParticipant#saving(org.eclipse.core.resources.ISaveContext)
	 */
	public void saving(ISaveContext context) throws CoreException {
		//Request a resource delta to be used on next activation.
	    context.needDelta();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.ISaveParticipant#doneSaving(org.eclipse.core.resources.ISaveContext)
	 */
	public void doneSaving(ISaveContext context) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.ISaveParticipant#prepareToSave(org.eclipse.core.resources.ISaveContext)
	 */
	public void prepareToSave(ISaveContext context) throws CoreException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.ISaveParticipant#rollback(org.eclipse.core.resources.ISaveContext)
	 */
	public void rollback(ISaveContext context) {
	}

}
