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
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Vector;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetListener;
import org.eclipse.cdt.make.core.IMakeTargetProvider;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeTargetEvent;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class MakeTargetProvider implements IMakeTargetProvider, IResourceChangeListener {
	private static String TARGET_BUILD_EXT = MakeCorePlugin.getUniqueIdentifier() + ".MakeTargetBuilder"; //$NON-NLS-1$

	private ListenerList listeners = new ListenerList();
	private HashMap projectMap = new HashMap();
	private HashMap builderMap;

	public MakeTargetProvider() {
	}

	public IMakeTarget addTarget(IContainer container, String targetBuilderID, String targetName) throws CoreException {
		if (container instanceof IWorkspaceRoot) {
			throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1, MakeCorePlugin.getResourceString("MakeTargetProvider.add_to_workspace_root"), null)); //$NON-NLS-1$
		}
		ProjectTargets projectTargets = (ProjectTargets)projectMap.get(container.getProject());
		if (projectTargets == null) {
			projectTargets = readTargets(container.getProject());
		}
		MakeTarget target = new MakeTarget(container, targetBuilderID, targetName);
		projectTargets.add(target);
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
		notifyListeners(new MakeTargetEvent(this, MakeTargetEvent.TARGET_REMOVED, target));
	}

	public void renameTarget(IMakeTarget target, String name) throws CoreException {
		IProject project = target.getContainer().getProject();
		ProjectTargets projectTargets = (ProjectTargets)projectMap.get(project);
		if (projectTargets == null) {
			projectTargets = readTargets(project);
		}
		if (!projectTargets.contains((MakeTarget)target)) {
			throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1, MakeCorePlugin.getResourceString("MakeTargetProvider.target_exists"), null)); //$NON-NLS-1$
		}
		((MakeTarget)target).setName(name);
		projectTargets.setDirty();
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
		Vector tProj = new Vector();
		IProject project[] = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < project.length; i++) {
			IProjectDescription description = project[i].getDescription();
			ICommand builder[] = description.getBuildSpec();
			for (int j = 0; j < builder.length; j++) {
				if (builderMap.containsValue(builder[j].getBuilderName())) {
					tProj.add(project[i]);
					break;
				}
			}
		}
		return (IProject[])tProj.toArray(new IProject[tProj.size()]);
	}

	public void startup() {
		initializeBuilders();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	public void shutdown() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	public void resourceChanged(IResourceChangeEvent event) {
		// dinglis-TODO listen for project that add/remove a target type builder
	}

	protected void writeTargets(ProjectTargets projectTargets) {
		IPath targetFilePath = MakeCorePlugin.getDefault().getStateLocation().append(projectTargets.getProject().getName());
		File targetFile = targetFilePath.toFile();
		try {

			FileOutputStream file = new FileOutputStream(targetFile);
		} catch (Exception e) {
		}
	}

	protected ProjectTargets readTargets(IProject project) throws CoreException {
		IPath targetFilePath = MakeCorePlugin.getDefault().getStateLocation().append(project.getName());
		File targetFile = targetFilePath.toFile();
		if (targetFile.exists()) {
			try {
				return new ProjectTargets(project, targetFile);
			} catch (Exception e) {
			}
		}
		return new ProjectTargets(project);
	}

	protected void initializeBuilders() {
		builderMap = new HashMap();

		IExtensionPoint point = MakeCorePlugin.getDefault().getDescriptor().getExtensionPoint(MakeTargetProvider.TARGET_BUILD_EXT);
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
}