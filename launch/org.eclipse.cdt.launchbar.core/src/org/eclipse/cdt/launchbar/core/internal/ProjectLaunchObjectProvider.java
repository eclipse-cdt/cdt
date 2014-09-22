/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer - Initial API and implementation
 *    Alena Laskavaia
 *******************************************************************************/
package org.eclipse.cdt.launchbar.core.internal;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchObjectProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

/**
 * Injects IProject objects from platform resources into the launch bar model for potential
 * project descriptors.
 */
public class ProjectLaunchObjectProvider implements ILaunchObjectProvider, IResourceChangeListener {
	private ILaunchBarManager manager;

	@Override
	public void init(ILaunchBarManager manager) throws CoreException {
		this.manager = manager;
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			manager.launchObjectAdded(project);
		}
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
	}

	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		try {
			event.getDelta().accept(new IResourceDeltaVisitor() {
				@Override
				public boolean visit(IResourceDelta delta) throws CoreException {
					IResource res = delta.getResource();
					if (res instanceof IProject) {
						IProject project = (IProject) res;
						int kind = delta.getKind();
						if ((kind & IResourceDelta.ADDED) != 0) {
							manager.launchObjectAdded(project);
						} else if ((kind & IResourceDelta.REMOVED) != 0) {
							manager.launchObjectRemoved(project);
						} else if ((kind & IResourceDelta.CHANGED) != 0) {
							// TODO may need to be more concise as to what changes we're looking for
							manager.launchObjectChanged(project);
						}
						return false;
					} else if (res instanceof IFile || res instanceof IFolder) {
						return false;
					}
					return true;
				}
			});
		} catch (CoreException e) {
			Activator.log(e.getStatus());
		}
	}
}
