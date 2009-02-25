/*******************************************************************************
 * Copyright (c) 2008 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.core.executables;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.executables.Executable;
import org.eclipse.cdt.debug.core.executables.ExecutablesManager;
import org.eclipse.cdt.debug.core.executables.IExecutableProvider;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;

public class StandardExecutableProvider implements IResourceChangeListener, ICProjectDescriptionListener, IExecutableProvider {

	private ArrayList<Executable> executables = new ArrayList<Executable>();

	public StandardExecutableProvider() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
		CoreModel.getDefault().getProjectDescriptionManager().addCProjectDescriptionListener(this,
				CProjectDescriptionEvent.DATA_APPLIED | CProjectDescriptionEvent.LOADED);
	}

	public void resourceChanged(IResourceChangeEvent event) {

		// refresh when projects are opened or closed. note that deleted
		// projects are handled later
		// in this method. new projects are handled in handleEvent.
		// resource changed events always start at the workspace root, so
		// projects
		// are the next level down
		IResourceDelta[] projects = event.getDelta().getAffectedChildren();
		for (IResourceDelta projectDelta : projects) {
			if ((projectDelta.getFlags() & IResourceDelta.OPEN) != 0) {
				if (projectDelta.getKind() == IResourceDelta.CHANGED) {
					// project was opened or closed
					ExecutablesManager.getExecutablesManager().scheduleRefresh(this, 0);
					return;
				}
			}
		}

		try {
			final StandardExecutableProvider provider = this;
			event.getDelta().accept(new IResourceDeltaVisitor() {

				public boolean visit(IResourceDelta delta) throws CoreException {
					if (delta.getKind() == IResourceDelta.ADDED || delta.getKind() == IResourceDelta.REMOVED) {
						IResource deltaResource = delta.getResource();
						if (deltaResource != null) {
							boolean refresh = false;
							if (delta.getKind() == IResourceDelta.REMOVED && deltaResource instanceof IProject) {
								// project deleted
								refresh = true;
							} else {
								// see if a binary has been added/removed
								IPath resourcePath = delta.getResource().getLocation();
								if (resourcePath != null && Executable.isExecutableFile(resourcePath)) {
									refresh = true;
								}
							}
							if (refresh) {
								ExecutablesManager.getExecutablesManager().scheduleRefresh(provider, 0);
								return false;
							}
						}
					}
					return true;
				}
			});
		} catch (CoreException e) {
		}
	}

	public void handleEvent(CProjectDescriptionEvent event) {
		// this handles the cases where the active build configuration changes,
		// and when new
		// projects are created.
		boolean refresh = false;

		int eventType = event.getEventType();

		if (eventType == CProjectDescriptionEvent.DATA_APPLIED) {
			// see if the active build config has changed
			ICProjectDescription newDesc = event.getNewCProjectDescription();
			ICProjectDescription oldDesc = event.getOldCProjectDescription();
			if (oldDesc != null && newDesc != null) {
				String newConfigName = newDesc.getActiveConfiguration().getName();
				String oldConfigName = oldDesc.getActiveConfiguration().getName();
				refresh = (!newConfigName.equals(oldConfigName));
			} else if (newDesc != null && oldDesc == null) {
				// project just created
				refresh = true;
			}
		}

		if (refresh) {
			ExecutablesManager.getExecutablesManager().scheduleRefresh(this, 0);
		}
	}

	public Executable[] getExecutables(IProgressMonitor monitor) {
		synchronized (executables) {
			executables.clear();

			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject[] projects = root.getProjects();

			monitor.beginTask("Checking C/C++ Projects", projects.length);

			for (IProject project : projects) {

				if (monitor.isCanceled())
					break;

				try {
					if (CoreModel.hasCNature(project)) {
						CModelManager manager = CModelManager.getDefault();
						ICProject cproject = manager.create(project);
						try {
							IBinary[] binaries = cproject.getBinaryContainer().getBinaries();
							for (IBinary binary : binaries) {
								if (binary.isExecutable() || binary.isSharedLib()) {
									IPath exePath = binary.getResource().getLocation();
									if (exePath == null)
										exePath = binary.getPath();
									Executable exe = new Executable(exePath, project, binary.getResource());
									executables.add(exe);
								}
							}
						} catch (CModelException e) {
						}
					}
				} catch (Exception e) {
					DebugPlugin.log( e );
				}
				monitor.worked(1);
			}
			monitor.done();
		}
		return executables.toArray(new Executable[executables.size()]);
	}

	public int getPriority() {
		return NORMAL_PRIORITY;
	}

	public IStatus removeExecutable(Executable executable, IProgressMonitor monitor) {
		IResource exeResource = executable.getResource();
		if (exeResource != null)
		{
			if (exeResource.isLinked())
			{
				try {
					exeResource.delete(true, monitor);
				} catch (CoreException e) {
					DebugPlugin.log( e );
				}				
			}
			return Status.OK_STATUS;
		}
		return new Status(IStatus.WARNING, CDebugCorePlugin.PLUGIN_ID, "Can't remove " + executable.getName() + ": it is built by project \"" + executable.getProject().getName() + "\"");
	}

}