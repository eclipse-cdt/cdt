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
package org.eclipse.cdt.managedbuilder.core;

import org.eclipse.cdt.core.settings.model.util.ResourceChangeHandlerBase;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.runtime.CoreException;

class ResourceChangeHandler2 extends ResourceChangeHandlerBase{
	private class RcMoveHandler implements IResourceMoveHandler {

		public void done() {
		}

		public void handleProjectClose(IProject project) {
			sendClose(project);
			try {
				ManagedBuildManager.setLoaddedBuildInfo(project, null);
			} catch (CoreException e) {
			}
		}

		public boolean handleResourceMove(IResource fromRc, IResource toRc) {
			switch(fromRc.getType()){
			case IResource.PROJECT:
				IProject fromProject = fromRc.getProject();
				IProject toProject = toRc.getProject();
				ManagedBuildInfo info = (ManagedBuildInfo)ManagedBuildManager.getBuildInfo(fromProject, false);
				if(info != null){
					info.updateOwner(toRc);
					ManagedBuildManager.updateLoaddedInfo(fromProject, toProject, info);
				}
			}
			return false;
		}

		public boolean handleResourceRemove(IResource rc) {
			switch(rc.getType()){
			case IResource.PROJECT:
				IProject project = rc.getProject();
				sendClose(project);
				try {
					ManagedBuildManager.setLoaddedBuildInfo(project, null);
				} catch (CoreException e) {
				}
			}
			return false;
		}
	}
	
	protected IResourceMoveHandler createResourceMoveHandler(
			IResourceChangeEvent event) {
		return new RcMoveHandler();
	}
	
	public void sendClose(IProject project){
		sendClose(ManagedBuildManager.getBuildInfo(project,false));
	}

	private void sendClose(IManagedBuildInfo info){
		if(info != null){
			IManagedProject managedProj = info.getManagedProject();
			if (managedProj != null) {
				IConfiguration cfgs[] = managedProj.getConfigurations();
			
				for(int i = 0; i < cfgs.length; i++)
					ManagedBuildManager.performValueHandlerEvent(cfgs[i], IManagedOptionValueHandler.EVENT_CLOSE, true);
			}
		}
	}


}
