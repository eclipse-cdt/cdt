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

public class ProjectLaunchObjectProvider implements ILaunchObjectProvider, IResourceChangeListener {

	private ILaunchBarManager manager;

	@Override
	public void init(ILaunchBarManager manager) {
		this.manager = manager;

		try {
			for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
				manager.launchObjectAdded(project);
			}
		} catch (CoreException e) {
			Activator.log(e.getStatus());
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
						IProject project = (IProject) delta.getResource();
						int kind = delta.getKind();
						if ((kind & IResourceDelta.ADDED) != 0) {
							manager.launchObjectAdded(project);
						} else if ((kind & IResourceDelta.REMOVED) != 0) {
							manager.launchObjectRemoved(project);
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
