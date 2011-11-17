/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Red Hat Inc. - add setTargets method
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

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

	private final ListenerList listeners = new ListenerList();
	private final Map<IProject, ProjectTargets> projectMap = new HashMap<IProject, ProjectTargets>();
	private HashMap<String, String> builderMap;
	protected Vector<IProject> fProjects = new Vector<IProject>();

	public MakeTargetManager() {
	}

	@Override
	public IMakeTarget createTarget(IProject project, String name, String targetBuilderID) throws CoreException {
		return new MakeTarget(this, project, targetBuilderID, name);
	}

	@Override
	public void addTarget(IMakeTarget target) throws CoreException {
		addTarget(null, target);
	}

	@Override
	public void addTarget(IContainer container, IMakeTarget target) throws CoreException {
		if (container instanceof IWorkspaceRoot) {
			throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1, MakeMessages.getString("MakeTargetManager.add_to_workspace_root"), null)); //$NON-NLS-1$
		}
		ProjectTargets projectTargets = projectMap.get(target.getProject());
		if (projectTargets == null) {
			projectTargets = readTargets(target.getProject());
		}
		target.setContainer(container == null ? target.getProject() : container);
		projectTargets.add(target);
		try {
			writeTargets(projectTargets);
		} catch (CoreException e) {
			projectTargets.remove(target);
			throw e;
		}
		notifyListeners(new MakeTargetEvent(this, MakeTargetEvent.TARGET_ADD, target));
	}

	@Override
	public void setTargets(IContainer container, IMakeTarget[] targets) throws CoreException {
		if (container instanceof IWorkspaceRoot) {
			throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1, MakeMessages.getString("MakeTargetManager.add_to_workspace_root"), null)); //$NON-NLS-1$
		}
		ProjectTargets projectTargets = projectMap.get(targets[0].getProject());
		if (projectTargets == null) {
			projectTargets = readTargets(targets[0].getProject());
		}
		if (container == null)
			container = targets[0].getProject();
		IMakeTarget[] oldTargets = projectTargets.get(container);
		projectTargets.set(container, targets);
		try {
			writeTargets(projectTargets);
		} catch (CoreException e) {
			// we only need to reset the targets if writing of targets fails
			projectTargets.set(container, oldTargets);
			throw e;
		}
		notifyListeners(new MakeTargetEvent(this, MakeTargetEvent.TARGET_ADD, targets[0]));
	}

	@Override
	public boolean targetExists(IMakeTarget target) {
		ProjectTargets projectTargets = projectMap.get(target.getProject());
		if (projectTargets == null) {
			projectTargets = readTargets(target.getProject());
		}
		return projectTargets.contains(target);
	}

	@Override
	public void removeTarget(IMakeTarget target) throws CoreException {
		ProjectTargets projectTargets = projectMap.get(target.getProject());
		if (projectTargets == null) {
			projectTargets = readTargets(target.getProject());
		}
		if (projectTargets.remove(target)) {
			try {
				writeTargets(projectTargets);
			} catch (CoreException e) {
				projectTargets.add(target);
				throw e;
			}
			notifyListeners(new MakeTargetEvent(this, MakeTargetEvent.TARGET_REMOVED, target));
		}
	}

	@Override
	public void renameTarget(IMakeTarget target, String name) throws CoreException {
		IMakeTarget makeTarget = target;

		ProjectTargets projectTargets = projectMap.get(makeTarget.getProject());
		if (projectTargets == null) {
			projectTargets = readTargets(makeTarget.getProject());
		}

		makeTarget.setName(name);
		if (projectTargets.contains(makeTarget)) {
			updateTarget(makeTarget);
		}
	}

	@Override
	public IMakeTarget[] getTargets(IContainer container) throws CoreException {
		ProjectTargets projectTargets = projectMap.get(container.getProject());
		if (projectTargets == null) {
			projectTargets = readTargets(container.getProject());
		}
		return projectTargets.get(container);
	}

	@Override
	public IMakeTarget findTarget(IContainer container, String name) throws CoreException {
		ProjectTargets projectTargets = projectMap.get(container.getProject());
		if (projectTargets == null) {
			projectTargets = readTargets(container.getProject());
		}
		return projectTargets.findTarget(container, name);
	}

	@Override
	public IProject[] getTargetBuilderProjects() {
		return fProjects.toArray(new IProject[fProjects.size()]);
	}

	@Override
	public String[] getTargetBuilders(IProject project) {
		if (fProjects.contains(project) || hasTargetBuilder(project)) {
			try {
				Vector<String> ids = new Vector<String>();
				IProjectDescription description = project.getDescription();
				ICommand commands[] = description.getBuildSpec();
				for (ICommand command : commands) {
					for (Entry<String, String> entry : builderMap.entrySet()) {
						if (entry.getValue().equals(command.getBuilderName())) {
							ids.add(entry.getKey());
						}
					}
				}
				return ids.toArray(new String[ids.size()]);
			} catch (CoreException e) {
			}
		}
		return new String[0];
	}

	@Override
	public boolean hasTargetBuilder(IProject project) {
		try {
			if (project.isAccessible()) {
				IProjectDescription description = project.getDescription();
				ICommand commands[] = description.getBuildSpec();
				for (ICommand command : commands) {
					if (builderMap.containsValue(command.getBuilderName())) {
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
		IProject projects[] = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject project : projects) {
			if (hasTargetBuilder(project)) {
				fProjects.add(project);
			}
		}
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	public void shutdown() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	@Override
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
		@Override
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

	protected void updateTarget(IMakeTarget target) throws CoreException {
	    if  (target.getContainer() != null ) { // target has not been added to manager.
			ProjectTargets projectTargets = projectMap.get(target.getProject());
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
		builderMap = new HashMap<String, String>();
        IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(MakeCorePlugin.PLUGIN_ID, MakeTargetManager.TARGET_BUILD_EXT);
		IExtension[] extensions = point.getExtensions();
		for (IExtension extension : extensions) {
			IConfigurationElement[] cfgElements = extension.getConfigurationElements();
			for (IConfigurationElement cfgElement : cfgElements) {
				if (cfgElement.getName().equals("builder")) { //$NON-NLS-1$
					String builderID = cfgElement.getAttribute("builderID"); //$NON-NLS-1$
					String targetID = cfgElement.getAttribute("id"); //$NON-NLS-1$
					builderMap.put(targetID, builderID);
				}
			}
		}
	}

	protected void notifyListeners(MakeTargetEvent event) {
		for (Object listener : listeners.getListeners()) {
			((IMakeTargetListener)listener).targetChanged(event);
		}
	}

	@Override
	public void addListener(IMakeTargetListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(IMakeTargetListener listener) {
		listeners.remove(listeners);
	}

	@Override
	public String getBuilderID(String targetBuilderID) {
		return builderMap.get(targetBuilderID);
	}
}
