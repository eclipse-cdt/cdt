/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * Broadcom Corporation - Bug 311189 and clean-up
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.ResourceChangeHandlerBase;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * This resource change handler notices external changes to the cdt projects
 * and associated project storage metadata files, as well as changes to
 * source folders
 *
 * Notifies CProjectDescriptionManager on some events, in particular project close and remove
 */
public class ResourceChangeHandler extends ResourceChangeHandlerBase implements  ISaveParticipant {

	/**
	 * A resource move handler which updates the model when C model resources are moved / removed.
	 * It's responsible for:
	 *   - Handling project description update after a project move
	 *   - Noticing the removal of directories that correspond to SourceEntrys
	 *   - Removing resource specific configuration from removed removed files and folders
	 *
	 * It records changes made during an IResourceChangeEvent for subsequent update to the model. This
	 * is performed in a WorkspaceJob to ensure we don't remove model entries while many changes are in
	 * progress as part of a team operation. See also Bug 311189
	 */
	private static class RcMoveHandler implements IResourceMoveHandler {
		CProjectDescriptionManager fMngr = CProjectDescriptionManager.getInstance();

		/** Map of modified project descriptions to update */
		Map<IProject, ICProjectDescription> fProjDesMap = new HashMap<IProject, ICProjectDescription>();
		/** Set of removed resources */
		Collection<IProject> fRemovedProjects = new HashSet<IProject>();
		/** Map of moved & removed resources: 'from' -> 'to'; 'to' may be null for removed resources */
		Map<IResource, IResource> fMovedResources = new HashMap<IResource, IResource>();

		public void handleProjectClose(IProject project) {
			fMngr.projectClosedRemove(project);
		}

		/**
		 * Check and return a new ICSourceEntry[] when a path has been changed in a project
		 * @param fromFullPath
		 * @param toFullPath
		 * @param entries - source entries to check
		 * @return ICSourceEntry[] or null if no change to the source entries
		 */
		private ICSourceEntry[] checkMove(IPath fromFullPath, IPath toFullPath, ICSourceEntry[] entries){
			boolean modified = false;
			for(int k = 0; k < entries.length; k++){
				if(entries[k].getFullPath().equals(fromFullPath)){
					ICSourceEntry entry = entries[k];
					entries[k] = (ICSourceEntry)CDataUtil.createEntry(entry.getKind(), toFullPath.toString(), null, entry.getExclusionPatterns(), entry.getFlags());
					modified = true;
				}
			}
			return modified ? entries : null;
		}

		/**
		 * Check and return a new ICSourceEntry[] which doesn't contain rcFullPath as a source path
		 * @param rcFullPath - that path being removed
		 * @param entries - source entries to check
		 * @return ICSourceEntry[] or null if no change
		 */
		private ICSourceEntry[] checkRemove(IPath rcFullPath, ICSourceEntry[] entries) {
			List<ICSourceEntry> updatedList = null;
			int num = 0;
			for (ICSourceEntry entrie : entries) {
				if(entrie.getFullPath().equals(rcFullPath)){
					if(updatedList == null){
						updatedList = new ArrayList<ICSourceEntry>(Arrays.asList(entries));
					}
					updatedList.remove(num);
				} else {
					num++;
				}
			}
			return updatedList != null ? updatedList.toArray(new ICSourceEntry[updatedList.size()]) : null;
		}

		public boolean handleResourceMove(IResource fromRc, IResource toRc) {
			boolean proceed = true;
			IProject fromProject = fromRc.getProject();
			IProject toProject = toRc.getProject();
			switch(toRc.getType()){
			case IResource.PROJECT:{
				ICProjectDescription des = fMngr.projectMove(fromProject, toProject);
				fRemovedProjects.add(fromProject);
				if(des != null)
					fProjDesMap.put(toProject, des);
			}
			break;
			case IResource.FOLDER:
			case IResource.FILE:
				// Only handle move in the same project 
				// TODO: should we treat this as a remove?
				if (!toProject.equals(fromProject))
					break;
				// If path hasn't changed, nothing to do
				if (fromRc.getFullPath().equals(toRc.getFullPath()))
					break;
				fMovedResources.put(fromRc, toRc);
				break;
			}

			return proceed;
		}

		private ICProjectDescription getProjectDescription(IResource rc) {
			IProject project = rc.getProject();
			ICProjectDescription des = fProjDesMap.get(project);
			if(des == null && !fProjDesMap.containsKey(project)){
				int flags = 0;
				flags |= CProjectDescriptionManager.INTERNAL_GET_IGNORE_CLOSE;
				flags |= ICProjectDescriptionManager.GET_WRITABLE;
				des = fMngr.getProjectDescription(project, flags);
				if(des != null)
					fProjDesMap.put(project, des);
			}
			return des;
		}

		public boolean handleResourceRemove(IResource rc) {
			boolean proceed = true;
			IProject project = rc.getProject();
			switch(rc.getType()){
			case IResource.PROJECT:
				fMngr.projectClosedRemove(project);
				fRemovedProjects.add(project);
				proceed = false;
				break;
			case IResource.FOLDER:
			case IResource.FILE:
				if (project.isAccessible())
					fMovedResources.put(rc, null);
				break;
			}

			return proceed;
		}

		public void done() {
			// If the resource's project was moved / removed, don't consider the path for source entry removal
			for (Iterator<IResource> it = fMovedResources.keySet().iterator(); it.hasNext() ; ) {
				if (fRemovedProjects.contains(it.next().getProject()))
					it.remove();
			}

			if (fMovedResources.isEmpty() && fProjDesMap.isEmpty())
				return;

			// Handle moved and removed resources
			//   - reconciles  both source-entry move & remove
			//   - resource configuration move & remove
			// Run it in the Workspace, so we don't trip Bug 311189
			CProjectDescriptionManager.runWspModification(new IWorkspaceRunnable(){

				public void run(IProgressMonitor monitor) throws CoreException {
					for (Map.Entry<IResource, IResource> entry : fMovedResources.entrySet()) {
						IResource from = entry.getKey();
						IResource to = entry.getValue();
						// TODO: don't handle moves to a different project
						assert(to == null || to.getProject().equals(from.getProject()));

						// Bug 311189 -- if the resource still exists now, don't treat as a remove!
						if (to == null) {
							if (from.getWorkspace().validateFiltered(from).isOK())
								from.refreshLocal(IResource.DEPTH_ZERO, null);
							if (from.exists())
								continue;
						}

						ICProjectDescription prjDesc = getProjectDescription(from);
						if (prjDesc == null)
							continue;

						for (ICConfigurationDescription cfg : prjDesc.getConfigurations()) {
							try {
								// Handle source entry change
								if (from instanceof IFolder) {
									ICSourceEntry[] entries = cfg.getSourceEntries();
									if (to != null)
										entries = checkMove(from.getFullPath(), to.getFullPath(), entries);
									else
										entries = checkRemove(from.getFullPath(), entries);
									// Update if there have been any changes
									if(entries != null)
										cfg.setSourceEntries(entries);
								}

								// We deliberately don't remove output entries. These directories may not exist when
								// the project is created and may be deleted at during a normal project lifecycle

								// Handle resource description change
								ICResourceDescription rcDescription = cfg.getResourceDescription(from.getProjectRelativePath(), true);
								if(rcDescription != null)
									if (to != null)
										rcDescription.setPath(to.getProjectRelativePath());
									else
										cfg.removeResourceDescription(rcDescription);
							} catch (WriteAccessException e) {
								CCorePlugin.log(e);
							} catch (CoreException e) {
								CCorePlugin.log(e);
							}
						}
					}
					fMovedResources.clear();

					// Save all the changed project descriptions
					for (Entry<IProject, ICProjectDescription> entry : fProjDesMap.entrySet()) {
						if(!entry.getKey().isAccessible())
							continue;
						try {
							fMngr.setProjectDescription(entry.getKey(), entry.getValue());
						} catch (CoreException e) {
							CCorePlugin.log(e);
						}
					}
				}
			}, new NullProgressMonitor());
		}
	}


	@Override
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

	@Override
	protected void doHandleResourceMove(IResourceChangeEvent event,
			IResourceMoveHandler handler) {
		switch(event.getType()){
		case IResourceChangeEvent.POST_CHANGE:
			IResourceDelta delta = event.getDelta();
			if(delta != null){
				IResourceDelta projs[] = delta.getAffectedChildren();
				for (IResourceDelta proj : projs) {
					IResourceDelta projDelta = proj;
					if(!shouldVisit((IProject)projDelta.getResource()))
						continue;

					if((projDelta.getKind() & IResourceDelta.REMOVED) == IResourceDelta.REMOVED)
						continue;

					IResourceDelta children[] = projDelta.getAffectedChildren();
					for (IResourceDelta child : children) {
						IResource rc = child.getResource();
						if(rc.getType() != IResource.FILE)
							continue;
					}
				}
			}
			break;
		}
		super.doHandleResourceMove(event, handler);
	}



}
