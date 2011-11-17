/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import java.io.File;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICDescriptionDelta;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.core.settings.model.util.ResourceChangeHandlerBase;
import org.eclipse.cdt.core.settings.model.util.ResourceChangeHandlerBase.IResourceMoveHandler;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

public class BuildStateManager {
	private static final String PREFS_LOCATION = "buildState"; //$NON-NLS-1$

	private static BuildStateManager fInstance;

	private EventListener fListener;

	private class ResourceMoveHandler implements IResourceMoveHandler {

		@Override
		public void done() {
		}

		@Override
		public void handleProjectClose(IProject project) {
		}

		@Override
		public boolean handleResourceMove(IResource fromRc, IResource toRc) {
			return doHandleResourceRemove(fromRc);
		}

		@Override
		public boolean handleResourceRemove(IResource rc) {
			return doHandleResourceRemove(rc);
		}

		private boolean doHandleResourceRemove(IResource rc){
			switch(rc.getType()){
			case IResource.PROJECT:
				removeProjectInfo(rc.getProject());
				return false;
			case IResource.ROOT:
				return true;
			//TODO: handle files and folder [re]move
			}
			return false;
		}
	}

	private class EventListener extends ResourceChangeHandlerBase implements ICProjectDescriptionListener {

		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			super.resourceChanged(event);
			//TODO: may handle resource changes as well
		}

		@Override
		public void handleEvent(CProjectDescriptionEvent event) {
			switch(event.getEventType()){
			case CProjectDescriptionEvent.APPLIED:
			case CProjectDescriptionEvent.LOADED:
				ICDescriptionDelta delta = event.getProjectDelta();
				processAppliedDelta(event, delta);
				break;
			}
		}

		private void processAppliedDelta(CProjectDescriptionEvent event, ICDescriptionDelta delta){
			if(delta == null)
				return;

			IProjectBuildState pbs = null;
			boolean apply = false;
			switch (delta.getDeltaKind()) {
			case ICDescriptionDelta.REMOVED:
				removeProjectInfo(event.getProject());
				break;
			case ICDescriptionDelta.CHANGED:
				ICDescriptionDelta[] children = delta.getChildren();
				for(int i = 0; i < children.length; i++){
					if(children[i].getDeltaKind() == ICDescriptionDelta.REMOVED){
						if(pbs == null){
							pbs = getProjectBuildState(event.getProject());
							String id = children[i].getSetting().getId();
							IConfigurationBuildState cbs = pbs.getConfigurationBuildState(id, false);
							if(cbs != null){
								apply = true;
								pbs.removeConfigurationBuildState(id);
							}
						}
					}
				}
				break;
			default:
				break;
			}

			if(pbs != null && apply){
				setProjectBuildState(event.getProject(), pbs);
			}
		}

		@Override
		protected IResourceMoveHandler createResourceMoveHandler(
				IResourceChangeEvent event) {
			return new ResourceMoveHandler();
		}

	}

	private void removeProjectInfo(IProject project){
		File f = getPrefsDir(project);
		if(f.exists()){
			File[] children = f.listFiles();
			for(int i = 0; i < children.length; i++){
				children[i].delete();
			}
			f.delete();
		}
	}

	private BuildStateManager(){
	}

	public static BuildStateManager getInstance(){
		if(fInstance == null)
			fInstance = new BuildStateManager();
		return fInstance;
	}

	public void startup(){
		if(fListener == null){
			fListener = new EventListener();
			CoreModel.getDefault().getProjectDescriptionManager().addCProjectDescriptionListener(fListener, CProjectDescriptionEvent.APPLIED | CProjectDescriptionEvent.LOADED);
			ResourcesPlugin.getWorkspace().addResourceChangeListener(fListener,
					IResourceChangeEvent.POST_CHANGE
					| IResourceChangeEvent.PRE_DELETE
					| IResourceChangeEvent.PRE_CLOSE);
		}
	}

	public void shutdown(){
		if(fListener != null){
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(fListener);
			CoreModel.getDefault().getProjectDescriptionManager().removeCProjectDescriptionListener(fListener);
		}
	}

	public IProjectBuildState getProjectBuildState(IProject project){
		return new ProjectBuildState(project);
	}

	public void setProjectBuildState(IProject project, IProjectBuildState state){
		((ProjectBuildState)state).serialize();
	}

	private IPath getPrefsDirPath(){
		IPath path = ManagedBuilderCorePlugin.getDefault().getStateLocation();
		path = path.append(PREFS_LOCATION);
		return path;
	}

	IPath getPrefsDirPath(IProject project){
		IPath path = getPrefsDirPath();
		path = path.append(project.getName());
		return path;
	}

	private File getPrefsDir(IProject project){
		IPath path = getPrefsDirPath(project);
		File file = path.toFile();
//		if(!file.exists())
//			file.mkdirs();
		return file;
	}
}
