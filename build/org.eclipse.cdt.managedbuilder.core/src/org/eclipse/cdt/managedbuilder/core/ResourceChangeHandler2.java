/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.settings.model.util.ResourceChangeHandlerBase;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.cdt.managedbuilder.internal.dataprovider.ConfigurationDataProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
class ResourceChangeHandler2 extends ResourceChangeHandlerBase{
	private class RcMoveHandler implements IResourceMoveHandler {

		@Override
		public void done() {
		}

		@Override
		public void handleProjectClose(IProject project) {
			sendClose(project);
			try {
				ManagedBuildManager.setLoaddedBuildInfo(project, null);
			} catch (CoreException e) {
			}
		}

		@Override
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

		@Override
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

	@Override
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

	private static class Visitor implements IResourceDeltaVisitor {
		private Set<IProject> fProjSet;

		Visitor(Set<IProject> projSet){
			fProjSet = projSet;
		}

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource rc = delta.getResource();
			switch (rc.getType()) {
			case IResource.ROOT:
				return true;
			case IResource.PROJECT:
				int flags = delta.getFlags();
				if((flags & IResourceDelta.DESCRIPTION) == IResourceDelta.DESCRIPTION){
					IProject project = rc.getProject();
					IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
					if(info != null && info.isValid() && info.getManagedProject() != null){
						IProjectDescription eDes = project.getDescription();
						IConfiguration[] cfgs = info.getManagedProject().getConfigurations();
						String natureIds[] = eDes.getNatureIds();
						for(int i = 0; i < cfgs.length; i++){
							String cachedIds[] = ConfigurationDataProvider.getNaturesIdsUsedOnCache(cfgs[i]);
							if(checkNaturesNeedUpdate(cachedIds, natureIds)){
								if(fProjSet == null)
									fProjSet = new HashSet<IProject>();

								fProjSet.add(project);
								break;
							}
						}
					}
				}
				return false;
			default:
				return false;
			}
		}

		Set<IProject> getProjSet(){
			return fProjSet;
		}

	}

	private static boolean checkNaturesNeedUpdate(String[] oldIds, String[] newIds){
		if(oldIds == null)
			return true;

		Set<String> oldSet = new HashSet<String>(Arrays.asList(oldIds));
		Set<String> oldSetCopy = new HashSet<String>(oldSet);
		Set<String> newSet = new HashSet<String>(Arrays.asList(newIds));
		oldSet.removeAll(newSet);
		newSet.removeAll(oldSetCopy);
		if(oldSet.contains(CProjectNature.C_NATURE_ID)
				|| oldSet.contains(CCProjectNature.CC_NATURE_ID)
				|| newSet.contains(CProjectNature.C_NATURE_ID)
				|| newSet.contains(CCProjectNature.CC_NATURE_ID))
			return true;

		return false;
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		super.resourceChanged(event);

		switch(event.getType()){
		case IResourceChangeEvent.POST_CHANGE:
			IResourceDelta delta = event.getDelta();
			if(delta != null){
				Visitor visitor = new Visitor(null);
				try {
					delta.accept(visitor);
				} catch (CoreException e) {
					ManagedBuilderCorePlugin.log(e);
				}
				postProcess(visitor.getProjSet());
			}
		}
	}

	private void postProcess(final Set<IProject> projSet){
		if(projSet == null || projSet.size() == 0)
			return;

		IWorkspace wsp = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = wsp.getRoot();

		Job job = new Job(ManagedMakeMessages.getString("ResourceChangeHandler2.0")){ //$NON-NLS-1$

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				for (IProject project : projSet) {
					try {
						ManagedBuildManager.updateCoreSettings(project);
					} catch (CoreException e) {
						ManagedBuilderCorePlugin.log(e);
					}
				}
				return Status.OK_STATUS;
			}

		};

		job.setRule(root);
		job.setSystem(true);
		job.schedule();
	}

}
