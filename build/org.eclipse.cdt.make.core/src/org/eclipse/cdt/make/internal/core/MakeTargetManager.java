/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetListener;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeTargetEvent;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class MakeTargetManager implements IMakeTargetManager, IResourceChangeListener {
	private static String TARGET_BUILD_EXT = "MakeTargetBuilder"; //$NON-NLS-1$

	private static String TARGETS_EXT = "targets"; //$NON-NLS-1$

	private ListenerList listeners = new ListenerList();
	Map projectMap = new HashMap();
	private HashMap builderMap;
	protected Vector fProjects = new Vector();

	public MakeTargetManager() {
	}

	public IMakeTarget createTarget(IProject project, String name, String targetBuilderID) throws CoreException {
		return new MakeTarget(this, project, targetBuilderID, name);
	}

	public void addTarget(IMakeTarget target) throws CoreException {
		addTarget(null, target);
	}
	
	public void addTarget(IContainer container, IMakeTarget target) throws CoreException {
		if (container instanceof IWorkspaceRoot) {
			throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1, MakeMessages.getString("MakeTargetManager.add_to_workspace_root"), null)); //$NON-NLS-1$
		}
		ProjectTargets projectTargets = (ProjectTargets)projectMap.get(target.getProject());
		if (projectTargets == null) {
			projectTargets = readTargets(target.getProject());
		}
		((MakeTarget) target).setContainer(container == null ? target.getProject() : container);
		projectTargets.add((MakeTarget) target);
		try {
			writeTargets(projectTargets);
		} catch (CoreException e) {
			projectTargets.remove((MakeTarget) target);
			throw e;
		}
		notifyListeners(new MakeTargetEvent(this, MakeTargetEvent.TARGET_ADD, target));
	}

	public boolean targetExists(IMakeTarget target) {
		ProjectTargets projectTargets = (ProjectTargets)projectMap.get(target.getProject());
		if (projectTargets == null) {
			projectTargets = readTargets(target.getProject());
		}
		return projectTargets.contains((MakeTarget) target);
	}
	
	public void removeTarget(IMakeTarget target) throws CoreException {
		ProjectTargets projectTargets = (ProjectTargets)projectMap.get(target.getProject());
		if (projectTargets == null) {
			projectTargets = readTargets(target.getProject());
		}
		if (projectTargets.remove((MakeTarget) target)) {
			try {
				writeTargets(projectTargets);
			} catch (CoreException e) {
				projectTargets.add((MakeTarget) target);
				throw e;
			}
			notifyListeners(new MakeTargetEvent(this, MakeTargetEvent.TARGET_REMOVED, target));
		}
	}

	public void renameTarget(IMakeTarget target, String name) throws CoreException {
		ProjectTargets projectTargets = (ProjectTargets)projectMap.get(target.getProject());
		if (projectTargets == null) {
			projectTargets = readTargets(target.getProject());
		}
		if (!projectTargets.contains((MakeTarget)target)) {
			throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1, MakeMessages.getString("MakeTargetManager.target_exists"), null)); //$NON-NLS-1$
		}
		((MakeTarget)target).setName(name);
		updateTarget((MakeTarget) target);
	}

	public IMakeTarget[] getTargets(IContainer container) throws CoreException {
		ProjectTargets projectTargets = (ProjectTargets)projectMap.get(container.getProject());
		if (projectTargets == null) {
			projectTargets = readTargets(container.getProject());
		}
		return projectTargets.get(container);
	}

	public IMakeTarget findTarget(IContainer container, String name) throws CoreException {
		ProjectTargets projectTargets = (ProjectTargets)projectMap.get(container.getProject());
		if (projectTargets == null) {
			projectTargets = readTargets(container.getProject());
		}
		return projectTargets.findTarget(container, name);
	}

	public IProject[] getTargetBuilderProjects() {
		return (IProject[])fProjects.toArray(new IProject[fProjects.size()]);
	}

	public String[] getTargetBuilders(IProject project) {
		if (fProjects.contains(project) || hasTargetBuilder(project)) {
			try {
				Vector ids = new Vector();
				IProjectDescription description = project.getDescription();
				ICommand builder[] = description.getBuildSpec();
				for (int i = 0; i < builder.length; i++) {
					Iterator entries = builderMap.entrySet().iterator();
					while (entries.hasNext()) {
						Map.Entry entry = (Entry)entries.next();
						if (entry.getValue().equals(builder[i].getBuilderName())) {
							ids.add(entry.getKey());
						}
					}
				}
				return (String[])ids.toArray(new String[ids.size()]);
			} catch (CoreException e) {
			}
		}
		return new String[0];
	}

	public boolean hasTargetBuilder(IProject project) {
		try {
			if (project.isAccessible()) {
				IProjectDescription description = project.getDescription();
				ICommand builder[] = description.getBuildSpec();
				for (int j = 0; j < builder.length; j++) {
					if (builderMap.containsValue(builder[j].getBuilderName())) {
						return true;
					}
				}
			}
		} catch (CoreException e) {
		}
		return false;
	}

	public void startup() {
		initializeBuilders();
		IProject project[] = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < project.length; i++) {
			if (hasTargetBuilder(project[i])) {
				fProjects.add(project[i]);
			}
		}
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	public void shutdown() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		if (delta != null) {
			try {
				delta.accept(new MakeTargetVisitor());
			} catch (CoreException e) {
				MakeCorePlugin.log(e);
			}
		}
	}

	class MakeTargetVisitor implements IResourceDeltaVisitor {
		/**
		 * @see IResourceDeltaVisitor#visit(IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) {
			if (delta == null) {
				return false;
			}
			IResource resource = delta.getResource();
			if (resource.getType() == IResource.PROJECT) {
				IProject project = (IProject)resource;
				int flags = delta.getFlags();
				int deltaKind = delta.getKind();
				if (deltaKind == IResourceDelta.ADDED) {
					if (hasTargetBuilder(project) && !fProjects.contains(project)) {
						fProjects.add(project);
						notifyListeners(new MakeTargetEvent(MakeTargetManager.this, MakeTargetEvent.PROJECT_ADDED, project));
					}
				} else if (deltaKind == IResourceDelta.REMOVED) {
					if (fProjects.contains(project)) {
						deleteTargets(project);
						fProjects.remove(project);
						notifyListeners(new MakeTargetEvent(MakeTargetManager.this, MakeTargetEvent.PROJECT_REMOVED, project));
					}
				} else if (deltaKind == IResourceDelta.CHANGED) {
					if (0 != (flags & IResourceDelta.DESCRIPTION)) {
						if (fProjects.contains(project) && !hasTargetBuilder(project)) {
							fProjects.remove(project);
							projectMap.remove(project);
							notifyListeners(new MakeTargetEvent(MakeTargetManager.this, MakeTargetEvent.PROJECT_REMOVED, project));
						} else if (!fProjects.contains(project) && hasTargetBuilder(project)) {
							fProjects.add(project);
							notifyListeners(new MakeTargetEvent(MakeTargetManager.this, MakeTargetEvent.PROJECT_ADDED, project));
						}
					}
					if (0 != (flags & IResourceDelta.OPEN)) {
						if (!project.isOpen() && fProjects.contains(project)) {
							fProjects.remove(project);
							projectMap.remove(project);
							notifyListeners(new MakeTargetEvent(MakeTargetManager.this, MakeTargetEvent.PROJECT_REMOVED, project));
						} else if (project.isOpen() && hasTargetBuilder(project) && !fProjects.contains(project)) {
							fProjects.add(project);
							notifyListeners(new MakeTargetEvent(MakeTargetManager.this, MakeTargetEvent.PROJECT_ADDED, project));
						}
					}
				}
				return false;
			}
			return resource instanceof IWorkspaceRoot;
		}
	}

	protected void updateTarget(MakeTarget target) throws CoreException {
	    if  (target.getContainer() != null ) { // target has not been added to manager.
			ProjectTargets projectTargets = (ProjectTargets)projectMap.get(target.getProject());
	    	if (projectTargets == null || !projectTargets.contains(target)) {
	    		return; // target has not been added to manager.
	    	}
	    	writeTargets(projectTargets);
	    	notifyListeners(new MakeTargetEvent(this, MakeTargetEvent.TARGET_CHANGED, target));
	    }
	}

	protected void writeTargets(ProjectTargets projectTargets) throws CoreException {
		projectTargets.saveTargets();
	}

	protected ProjectTargets readTargets(IProject project) {
		ProjectTargets projectTargets = new ProjectTargets(this, project);
		projectMap.put(project, projectTargets);
		return projectTargets;
	}

	protected void deleteTargets(IProject project) {
		//Historical: We clean up after all other parts.
		IPath targetFilePath =
			MakeCorePlugin.getDefault().getStateLocation().append(project.getName()).addFileExtension(TARGETS_EXT);
		File targetFile = targetFilePath.toFile();
		if (targetFile.exists()) {
			targetFile.delete();
		}
		projectMap.remove(project);
	}

	protected void initializeBuilders() {
		builderMap = new HashMap();
        IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(MakeCorePlugin.PLUGIN_ID, MakeTargetManager.TARGET_BUILD_EXT);
		IExtension[] ext = point.getExtensions();
		for (int i = 0; i < ext.length; i++) {
			IConfigurationElement[] element = ext[i].getConfigurationElements();
			for (int j = 0; j < element.length; j++) {
				if (element[j].getName().equals("builder")) { //$NON-NLS-1$
					String builderID = element[j].getAttribute("builderID"); //$NON-NLS-1$
					String targetID = element[j].getAttribute("id"); //$NON-NLS-1$
					builderMap.put(targetID, builderID);
				}
			}
		}
	}

	protected void notifyListeners(MakeTargetEvent event) {
		Object[] list = listeners.getListeners();
		for (int i = 0; i < list.length; i++) {
			((IMakeTargetListener)list[i]).targetChanged(event);
		}
	}

	public void addListener(IMakeTargetListener listener) {
		listeners.add(listener);
	}

	public void removeListener(IMakeTargetListener listener) {
		listeners.remove(listeners);
	}

	public String getBuilderID(String targetBuilderID) {
		return (String)builderMap.get(targetBuilderID);
	}
}
