package org.eclipse.cdt.internal.ui.makeview;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;

public class MakeContentProvider implements ITreeContentProvider, IResourceChangeListener {

	protected Viewer viewer;

	/**
	 * Constructor for MakeContentProvider
	 */
	public MakeContentProvider() {
		super();
	}

	/**
	 * @see ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object obj) {
		if (obj instanceof MakeTarget) {
			MakeTarget md = (MakeTarget)obj;
			return (Object[])md.getChildren();
		}
		return new Object[0];
	}

	/**
	 * @see ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object obj) {
		if (obj instanceof MakeTarget) {
			MakeTarget directives = (MakeTarget)obj;
			return directives.getParent();
		}
		return null;
	}

	/**
	 * @see ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object obj) {
		return getChildren(obj).length > 0;
	}

	/**
	 * @see IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object obj) {
		return getChildren(obj);
	}

	/**
	 * @see IContentProvider#dispose()
	 */
	public void dispose() {
		if (viewer != null) {
			Object obj = viewer.getInput();
			if (obj instanceof MakeTarget) {
				MakeTarget target = (MakeTarget)obj;
				IWorkspace workspace = target.getResource().getWorkspace();
				workspace.removeResourceChangeListener(this);
			}
		}
	}

	/**
	 * @see IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
		if (oldInput != null) {
			if (oldInput instanceof MakeTarget) {
				IResource res = ((MakeTarget)oldInput).getResource();
				if (res instanceof IWorkspaceRoot) {
					IWorkspace workspace = res.getWorkspace();
					workspace.removeResourceChangeListener(this);
				}
			}
		}
		if (newInput != null) {
			if (newInput instanceof MakeTarget) {
				IResource res = ((MakeTarget)newInput).getResource();
				if (res instanceof IWorkspaceRoot) {
					IWorkspace workspace = res.getWorkspace();
					workspace.addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
				}
			}
		}
	}

	public void resourceChanged (final IResourceChangeEvent event) {
		final IResourceDelta deltas = event.getDelta();
		Control ctrl = viewer.getControl();
		if (ctrl != null && !ctrl.isDisposed()) {
	        // Get the affected resource
			ctrl.getDisplay().syncExec(new Runnable() {
				public void run() {
					processDelta (deltas);
				}
			});
		}
	}

	void processDelta (IResourceDelta delta) {
		// Bail out if the widget was disposed.
		Control ctrl = viewer.getControl();
		if (ctrl == null || ctrl.isDisposed()) {
			return;
		}

		if (delta == null) {
			return;
		}

		int changeFlags = delta.getFlags();

        IResourceDelta[] affectedChildren =
            delta.getAffectedChildren(IResourceDelta.CHANGED);

		// Not interested in Content changes.
        for (int i = 0; i < affectedChildren.length; i++) {
            if ((affectedChildren[i].getFlags() & IResourceDelta.TYPE) != 0) {
                return;
			}
		}

		// handle open and closing.
        if ((changeFlags & (IResourceDelta.OPEN | IResourceDelta.SYNC)) != 0) {
				ctrl.setRedraw(false);
                viewer.refresh();
				ctrl.setRedraw(true);
				return;
        }

		// Handle changed children recursively.
		for (int i = 0; i < affectedChildren.length; i++) {
			processDelta(affectedChildren[i]);
		}

		// We are only interested in creation and deletion of folders.
		affectedChildren = delta.getAffectedChildren(IResourceDelta.REMOVED | IResourceDelta.ADDED);
		if (affectedChildren.length > 0) {
			for (int i = 0; i < affectedChildren.length; i++) {
				IResource r = affectedChildren[i].getResource();
				if (r instanceof IContainer) {
					ctrl.setRedraw(false);
					viewer.refresh();
					ctrl.setRedraw(true);
					break;
				}
			}
		}
	}
}
