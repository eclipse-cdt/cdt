package org.eclipse.cdt.internal.ui.makeview;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;

import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.resources.MakeUtil;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;


/**
 * A leaf is a IResource with a Make Directive.
 * We use the term Directive instead of Make Targets
 * to not confuse with remote targets.
 */

public class MakeTarget implements IAdaptable {

    private static final MakeTarget[] emptyArray= new MakeTarget[0];
    private IResource resource;
    private String target;

    public MakeTarget(IResource res) {
		this(res, "");
    }

    public MakeTarget(IResource res, String goals) {
		resource = res;
		target = (goals == null) ? "" : goals;
    }

    /**
     * @see IAdaptable#getAdapter(Object)
     */
    public Object getAdapter(Class adapter) {
		if (adapter == IResource.class) {
			return resource;
		}
		return null;
	}

    /**
     */
    public MakeTarget[] getChildren() {
		if (resource != null && target.length() == 0) {
			ArrayList list = new ArrayList();
			if (resource instanceof IWorkspaceRoot) {
				IWorkspaceRoot root = (IWorkspaceRoot)resource;
				IProject [] projects = root.getProjects();
				for (int i = 0; i < projects.length; i++) {
					if (projects[i].isOpen()) {
						try {
							if (projects[i].hasNature(CProjectNature.C_NATURE_ID)) {
								list.add (new MakeTarget(projects[i]));
							}
						} catch (CoreException e) {
						}
					}
				}
			} else if (resource instanceof IContainer) {
				IContainer container = (IContainer)resource;
				try {
					IResource[] resources = container.members();
					for (int i = 0; i < resources.length; i++) {
						if (resources[i] instanceof IContainer) {
							list.add (new MakeTarget(resources[i]));
						}
					}
				} catch (CoreException e) {
				}
				String [] targets = MakeUtil.getPersistentTargets(resource);
				for (int i = 0; i < targets.length; i++) {
					if (targets[i] == null)
						targets[i] = "";
					list.add (new MakeTarget(resource, targets[i]));
				}
			}
			return (MakeTarget[])list.toArray(emptyArray);
		}
		return emptyArray;
	}

    /**
     */
    public MakeTarget getParent() {
		if (target.length() == 0)
			return new MakeTarget(resource.getParent());
		return new MakeTarget(resource);
    }

    public String toString() {
		if (target.length() == 0)
			return resource.getName();
		return target;
    }

	public ImageDescriptor getImageDescriptor() {
		if (isLeaf()) {
			return CPluginImages.DESC_BUILD_MENU;
		}
		IWorkbenchAdapter adapter = (IWorkbenchAdapter)((IAdaptable)resource).getAdapter(IWorkbenchAdapter.class);
		if (adapter == null)
			return null;
		return adapter.getImageDescriptor(resource);
	}

    public IResource getResource () {
		return resource;
    }

    public boolean isLeaf () {
		return (target.length() != 0);
    }

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		if (null == obj)
			return false;

		if (!(obj instanceof MakeTarget))
			return false;

		MakeTarget other = (MakeTarget)obj;
		return (resource.equals(other.resource) && target.equals(other.target));
	}
}
