/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.eclipse.core.runtime.Status;

public class MakeTargetManager implements IMakeTargetManager, IResourceChangeListener {
	private static String TARGET_BUILD_EXT = "MakeTargetBuilder"; //$NON-NLS-1$

	private ListenerList listeners = new ListenerList();
	private HashMap projectMap = new HashMap();
	private HashMap builderMap;
	private Vector fProjects = new Vector();

	public MakeTargetManager() {
	}

	public IMakeTarget addTarget(IContainer container, String targetBuilderID, String targetName) throws CoreException {
		if (container instanceof IWorkspaceRoot) {
			throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1, MakeCorePlugin.getResourceString("MakeTargetManager.add_to_workspace_root"), null)); //$NON-NLS-1$
		}
		ProjectTargets projectTargets = (ProjectTargets)projectMap.get(container.getProject());
		if (projectTargets == null) {
			projectTargets = readTargets(container.getProject());
		}
		MakeTarget target = new MakeTarget(container, targetBuilderID, targetName);
		projectTargets.add(target);
		writeTargets(projectTargets);
		notifyListeners(new MakeTargetEvent(this, MakeTargetEvent.TARGET_ADD, target));
		return target;
	}

	public void removeTarget(IMakeTarget target) throws CoreException {
		IProject project = target.getContainer().getProject();
		ProjectTargets projectTargets = (ProjectTargets)projectMap.get(project);
		if (projectTargets == null) {
			projectTargets = readTargets(project);
		}
		projectTargets.remove(target);
		writeTargets(projectTargets);
		notifyListeners(new MakeTargetEvent(this, MakeTargetEvent.TARGET_REMOVED, target));
	}

	public void renameTarget(IMakeTarget target, String name) throws CoreException {
		IProject project = target.getContainer().getProject();
		ProjectTargets projectTargets = (ProjectTargets)projectMap.get(project);
		if (projectTargets == null) {
			projectTargets = readTargets(project);
		}
		if (!projectTargets.contains((MakeTarget)target)) {
			throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1, MakeCorePlugin.getResourceString("MakeTargetManager.target_exists"), null)); //$NON-NLS-1$
		}
		((MakeTarget)target).setName(name);
		writeTargets(projectTargets);
		notifyListeners(new MakeTargetEvent(this, MakeTargetEvent.TARGET_CHANGED, target));
	}

	public IMakeTarget[] getTargets(IContainer container) throws CoreException {
		ProjectTargets projectTargets = (ProjectTargets)projectMap.get(container.getProject());
		if (projectTargets == null) {
			projectTargets = readTargets(container.getProject());
		}
		return projectTargets.get(container);
	}

	public IProject[] getTargetBuilderProjects() throws CoreException {
		return (IProject[])fProjects.toArray(new IProject[fProjects.size()]);
	}

	public String[] getTargetBuilders(IProject project) {
		if (fProjects.contains(project)) {
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

	protected boolean hasTargetBuilder(IProject project) {
		try {
			IProjectDescription description = project.getDescription();
			ICommand builder[] = description.getBuildSpec();
			for (int j = 0; j < builder.length; j++) {
				if (builderMap.containsValue(builder[j].getBuilderName())) {
					return true;
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
				break;
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
					if (hasTargetBuilder(project)) {
						fProjects.add(project);
						notifyListeners(new MakeTargetEvent(MakeTargetManager.this, MakeTargetEvent.PROJECT_ADDED, project));
					}
				} else if (deltaKind == IResourceDelta.REMOVED) {
					if (fProjects.contains(project)) {
						fProjects.remove(project);
						notifyListeners(new MakeTargetEvent(MakeTargetManager.this, MakeTargetEvent.PROJECT_REMOVED, project));
					}
				} else if (deltaKind == IResourceDelta.CHANGED) {
					if (0 != (flags & IResourceDelta.DESCRIPTION)) {
						if (fProjects.contains(project) && !hasTargetBuilder(project)) {
							fProjects.remove(project);
							notifyListeners(new MakeTargetEvent(MakeTargetManager.this, MakeTargetEvent.PROJECT_REMOVED, project));
						} else if (!fProjects.contains(project) && hasTargetBuilder(project)) {
							fProjects.add(project);
							notifyListeners(new MakeTargetEvent(MakeTargetManager.this, MakeTargetEvent.PROJECT_ADDED, project));
						}
					}
					if (0 != (flags & IResourceDelta.OPEN)) {
						if (!project.isOpen() && fProjects.contains(project)) {
							fProjects.remove(project);
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

	protected void writeTargets(ProjectTargets projectTargets) throws CoreException {
		IPath targetFilePath = MakeCorePlugin.getDefault().getStateLocation().append(projectTargets.getProject().getName());
		File targetFile = targetFilePath.toFile();
		try {
			FileOutputStream file = new FileOutputStream(targetFile);
			projectTargets.saveTargets(file);
		} catch (IOException e) {
			throw new CoreException(
				new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1, "Error writing target file", e));
		}

	}

	protected ProjectTargets readTargets(IProject project) throws CoreException {
		IPath targetFilePath = MakeCorePlugin.getDefault().getStateLocation().append(project.getName());
		File targetFile = targetFilePath.toFile();
		if (targetFile.exists()) {
			try {
				return new ProjectTargets(project, new FileInputStream(targetFile));
			} catch (FileNotFoundException e) {
				throw new CoreException(
					new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1, "Error reading target file", e));
			}
		}
		return new ProjectTargets(project);
	}

	protected void initializeBuilders() {
		builderMap = new HashMap();
		IExtensionPoint point = MakeCorePlugin.getDefault().getDescriptor().getExtensionPoint(MakeTargetManager.TARGET_BUILD_EXT);
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