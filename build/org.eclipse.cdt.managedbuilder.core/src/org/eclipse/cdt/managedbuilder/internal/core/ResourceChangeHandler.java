/*******************************************************************************
 * Copyright (c) 2005, 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;

public class ResourceChangeHandler implements IResourceChangeListener, ISaveParticipant {
	
	private Map fRmProjectToBuildInfoMap = new HashMap();

	private class ResourceConfigurationChecker implements IResourceDeltaVisitor{
		private IResourceDelta fRootDelta;
		private HashMap fBuildFileGeneratorMap = new HashMap();
		private HashSet fValidatedFilesSet = new HashSet();
		private HashSet fModifiedProjects = new HashSet();

		public ResourceConfigurationChecker(IResourceDelta rootDelta){
			fRootDelta = rootDelta;
		}
		
		public IProject[] getModifiedProjects(){
			return (IProject[])fModifiedProjects.toArray(new IProject[fModifiedProjects.size()]);
		}

		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource dResource = delta.getResource();
			int rcType = dResource.getType(); 
			
			if(rcType == IResource.PROJECT || rcType == IResource.FOLDER){
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IProject project = null;
				IResource rcToCheck = null;
				switch (delta.getKind()) {
				case IResourceDelta.REMOVED :
					if (rcType == IResource.PROJECT){
						IManagedBuildInfo info = (IManagedBuildInfo)fRmProjectToBuildInfoMap.remove(dResource);

						if((delta.getFlags() & IResourceDelta.MOVED_TO) == 0) {
							if(info != null){
								sendClose(info);
								PropertyManager.getInstance().clearProperties(info.getManagedProject());
							}
							break;
						}
					}
				case IResourceDelta.CHANGED :
					if ((delta.getFlags() & IResourceDelta.MOVED_TO) != 0) {
						IPath path = delta.getMovedToPath();
						if(path != null){
							project = root.findMember(path.segment(0)).getProject();
							if(project != null && rcType == IResource.FOLDER)
								rcToCheck = root.getFolder(substituteProject(dResource.getFullPath(),project.getName()));
						}
						break;
					}
				default:
					project = dResource.getProject();
					if(rcType == IResource.FOLDER)
						rcToCheck = dResource;
					break;
				}

				if(project != null) {
					IManagedBuilderMakefileGenerator makeGen = getInitializedGenerator(project);
					if(makeGen != null){
						if(rcToCheck == null || !makeGen.isGeneratedResource(rcToCheck))
							return true;
					}
				}
				return false;
			} else if (rcType == IResource.FILE && !dResource.isDerived()) {
				int flags = delta.getFlags();
				switch (delta.getKind()) {
				case IResourceDelta.REMOVED :
					if ((flags & IResourceDelta.MOVED_TO) == 0) {
						handleDeleteFile(dResource.getFullPath());
						break;
					}
				case IResourceDelta.ADDED :
				case IResourceDelta.CHANGED :
				    if ((flags & IResourceDelta.MOVED_TO) != 0) {
				    	IPath path = delta.getMovedToPath();
				    	if (path != null) {
				    		handleRenamedFile(
				    				  dResource.getFullPath(),
				    				  path);
				    	}
				    } else if ((flags & IResourceDelta.MOVED_FROM) != 0) {
						IPath path = delta.getMovedFromPath();
				    	if (path != null) {
				    		handleRenamedFile(
				    				  path,
				    				  dResource.getFullPath());
				    	}
				    }
					break;

				default:
					break;
				}
				return false;
			}
			return true;	//  visit the children
		}
		
		private IPath substituteProject(IPath path, String projectName){
			return new Path(projectName).makeAbsolute().append(path.removeFirstSegments(1));
		}

		private void handleRenamedFile(IPath fromPath, IPath toPath){
			if(!fValidatedFilesSet.add(fromPath))
				return;

			IProject fromProject = findModifiedProject(fromPath.segment(0));
			if(fromProject == null)
				return;
			IManagedBuilderMakefileGenerator fromMakeGen = getInitializedGenerator(fromProject);
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			if(fromMakeGen == null || fromMakeGen.isGeneratedResource(root.getFile(substituteProject(fromPath,fromProject.getName()))))
				return;
			
			IManagedBuildInfo fromInfo = fromProject != null ?
					ManagedBuildManager.getBuildInfo(fromProject) :
						null;

			IProject toProject = root.findMember(toPath.uptoSegment(1)).getProject();
			IManagedBuildInfo toInfo = toProject != null ? 
					ManagedBuildManager.getBuildInfo(toProject) :
						null;
			IManagedBuilderMakefileGenerator toMakeGen = toProject != null ? 
					getInitializedGenerator(toProject) :
						null;
			if(toMakeGen != null && toMakeGen.isGeneratedResource(root.getFile(toPath)))
				toInfo = null;

			if(fromInfo == toInfo){
				//the resource was moved whithing the project scope
				if(updateResourceConfigurations(fromInfo,fromPath,toPath) && toProject != null)
					fModifiedProjects.add(toProject);
			} else {
				if(fromInfo != null && toInfo != null){
					//TODO: this is the case when the resource
					//is moved from one managed project to another
					//should we handle this?
					//e.g. add resource configurations to the destination project?
				}
				if(fromInfo != null && removeResourceConfigurations(fromInfo,fromPath) && fromProject != null)
					fModifiedProjects.add(fromProject);
			}
		}
		
		private void handleDeleteFile(IPath path){
			IProject project = findModifiedProject(path.segment(0));
			if(project != null){
				IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
				if(info != null 
						&& removeResourceConfigurations(info,path))
					fModifiedProjects.add(project);
			}
		}
		
		//finds the project geven the initial project name
		//That is:
		// if the project of a given name was renamed returns the renamed project
		// if the project of a given name was removed returns null
		// if the project of a given name was neither renamed or removed 
		//   returns the project of that name or null if the project does not exist
		//
		private IProject findModifiedProject(final String oldProjectName){
			IResourceDelta projectDelta = fRootDelta.findMember(new Path(oldProjectName));
			boolean replaced = false;
			if(projectDelta != null) {
				switch(projectDelta.getKind()){
					case IResourceDelta.REMOVED :
					    if ((projectDelta.getFlags() & IResourceDelta.MOVED_TO) == 0) {
					    	return null;
					    }
					case IResourceDelta.CHANGED :
					    if ((projectDelta.getFlags() & IResourceDelta.MOVED_TO) != 0) {
					    	IPath path = projectDelta.getMovedToPath();
					    	if(path != null)
					    		return ResourcesPlugin.getWorkspace().getRoot().findMember(path).getProject();
					    }
					    break;
				}
			}

			final IProject project[] = new IProject[1];
			try {
				fRootDelta.accept(new IResourceDeltaVisitor() {
					public boolean visit(IResourceDelta delta) throws CoreException {
						IResource dResource = delta.getResource();
						int rcType = dResource.getType();
						if(rcType == IResource.ROOT) {
							return true;
						} else if(rcType == IResource.PROJECT){
							switch(delta.getKind()){
							case IResourceDelta.ADDED :
							case IResourceDelta.CHANGED :
						      if ((delta.getFlags() & IResourceDelta.MOVED_FROM) != 0) {
						    	  IPath path = delta.getMovedFromPath();
						    	  if (path != null && path.segment(0).equals(oldProjectName)) {
						    		  project[0] = dResource.getProject();
						    	  }
						      }
							break;
							default:
								break;
							}
						}
						return false;
					}
				});
			} catch (CoreException e) {
			}

			if(project[0] == null && !replaced)
				project[0] = ResourcesPlugin.getWorkspace().getRoot().findMember(oldProjectName).getProject();
			return project[0];
		}

		private IManagedBuilderMakefileGenerator getInitializedGenerator(IProject project){
			IManagedBuilderMakefileGenerator makeGen = (IManagedBuilderMakefileGenerator)fBuildFileGeneratorMap.get(project);
			if (makeGen == null) {
				try {
					if (project.hasNature(ManagedCProjectNature.MNG_NATURE_ID)) {
						// Determine if we can access the build info before actually trying
						// If not, don't try, to avoid putting up a dialog box warning the user
						if (!ManagedBuildManager.canGetBuildInfo(project)) return null;
						
						IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
						if (buildInfo != null){
							IConfiguration defaultCfg = buildInfo.getDefaultConfiguration();
							if (defaultCfg != null) {
								makeGen = ManagedBuildManager.getBuildfileGenerator(defaultCfg);
								makeGen.initialize(project,buildInfo,new NullProgressMonitor());
								fBuildFileGeneratorMap.put(project,makeGen);
							}
						}
					}
				} catch (CoreException e){
					return null;
				}
			}
			return makeGen;
		}
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

	/*
	 *  I R e s o u r c e C h a n g e L i s t e n e r 
	 */

	/* (non-Javadoc)
	 * 
	 *  Handle the renaming and deletion of project resources
	 *  This is necessary in order to update ResourceConfigurations and AdditionalInputs
	 *  
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getSource() instanceof IWorkspace) {
			
			switch (event.getType()) {
				case IResourceChangeEvent.PRE_CLOSE:
					IResource proj = event.getResource();
					if(proj instanceof IProject)
						sendClose((IProject)proj);
					break;
				case IResourceChangeEvent.PRE_DELETE :
					IResource rc = event.getResource();
					if(rc instanceof IProject){
						IProject project = (IProject)rc;
						try {
							if (project.hasNature(ManagedCProjectNature.MNG_NATURE_ID)) {
								IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
								if(info != null)
									fRmProjectToBuildInfoMap.put(project, info);
							}
						} catch (CoreException e) {
						}
					}
				case IResourceChangeEvent.POST_CHANGE :
				case IResourceChangeEvent.POST_BUILD :
					IResourceDelta resDelta = event.getDelta();
					if (resDelta == null) {
						break;
					}
					try {
						ResourceConfigurationChecker rcChecker = new ResourceConfigurationChecker(resDelta);
						resDelta.accept(rcChecker);
						
						//saving info for the modified projects 
						initInfoSerialization(rcChecker.getModifiedProjects());
					
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
					break;
				default :
					break;
			}
		}
	}
	
	private void initInfoSerialization(final IProject projects[]){
		if(projects.length == 0)
			return;
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IResourceRuleFactory ruleFactory = workspace.getRuleFactory();
		ISchedulingRule buildInfoSaveRule;
		if(projects.length == 1){
			buildInfoSaveRule = ruleFactory.modifyRule(projects[0]);
		} else {
			ISchedulingRule rules[] = new ISchedulingRule[projects.length];
			for(int i = 0; i < rules.length; i++)
				rules[i] = ruleFactory.modifyRule(projects[i]);
			buildInfoSaveRule = MultiRule.combine(rules);
		}

		Job savingJob = new Job(ManagedMakeMessages.getResourceString("ResourceChangeHandler.buildInfoSerializationJob")){ 	//$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				for(int i = 0; i < projects.length; i++){
					ManagedBuildManager.saveBuildInfo(projects[i],true);
				}
				return new Status(
						IStatus.OK,
						ManagedBuilderCorePlugin.getUniqueIdentifier(),
						IStatus.OK,
						new String(),
						null);
			}
		};
		savingJob.setRule(buildInfoSaveRule);
		
		savingJob.schedule();
	}

	private boolean updateResourceConfigurations(IManagedBuildInfo info, IPath oldPath, IPath newPath){
		boolean changed = false;
		if(!oldPath.equals(newPath)){
			IManagedProject mngProj = info.getManagedProject();
			if(mngProj != null){
				IConfiguration configs[] = mngProj.getConfigurations();
				if(configs != null && configs.length > 0){
					for(int i = 0; i < configs.length; i++){
						if(updateResourceConfiguration(configs[i],oldPath,newPath))
							changed = true;
					}
				}
			}
		}
		return changed;
	}
	
	private boolean removeResourceConfigurations(IManagedBuildInfo info, IPath path){
		boolean changed = false;
		IManagedProject mngProj = info.getManagedProject();
		if(mngProj != null){
			IConfiguration configs[] = mngProj.getConfigurations();
			if(configs != null && configs.length > 0){
				for(int i = 0; i < configs.length; i++){
					if(removeResourceConfiguration(configs[i],path))
						changed = true;
				}
			}
		}
		return changed;
	}
	
	private boolean updateResourceConfiguration(IConfiguration config, IPath oldPath, IPath newPath){
		IResourceConfiguration rcCfg = config.getResourceConfiguration(oldPath.toString());
		if(rcCfg != null && !oldPath.equals(newPath)){
			config.removeResourceConfiguration(rcCfg);
			rcCfg.setResourcePath(newPath.toString());
			rcCfg.setRebuildState(true);
			((Configuration)config).addResourceConfiguration((ResourceConfiguration)rcCfg);
//			config.setRebuildState(true);
			return true;
		}
		return false;
	}
	
	private boolean removeResourceConfiguration(IConfiguration config, IPath path){
		IResourceConfiguration rcCfg = config.getResourceConfiguration(path.toString());
		if(rcCfg != null){
			config.removeResourceConfiguration(rcCfg);
//			config.setRebuildState(true);
			return true;
		}
		return false;
	}

	/*
	 *  I S a v e P a r t i c i p a n t 
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.ISaveParticipant#saving(org.eclipse.core.resources.ISaveContext)
	 */
	public void saving(ISaveContext context) throws CoreException {
		PropertyManager.getInstance().serialize();

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
