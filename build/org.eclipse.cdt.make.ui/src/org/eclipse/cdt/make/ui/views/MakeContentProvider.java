package org.eclipse.cdt.make.ui.views;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetListener;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeTargetEvent;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;

public class MakeContentProvider implements ITreeContentProvider, IMakeTargetListener, IResourceChangeListener {
	protected TreeViewer viewer;

	/**
	 * Constructor for MakeContentProvider
	 */
	public MakeContentProvider() {
	}

	public Object[] getChildren(Object obj) {
		if (obj instanceof IWorkspaceRoot) {
			try {
				return MakeCorePlugin.getDefault().getTargetProvider().getTargetBuilderProjects();
			} catch (CoreException e) {
			}
		} else if (obj instanceof IContainer) {
			ArrayList children = new ArrayList();
			try {
				IResource[] resource = ((IContainer) obj).members();
				for (int i = 0; i < resource.length; i++) {
					if (resource[i] instanceof IContainer) {
						children.add(resource[i]);
					}
				}
				children.addAll(Arrays.asList(MakeCorePlugin.getDefault().getTargetProvider().getTargets((IContainer) obj)));
			} catch (CoreException e) {
			}
			return children.toArray();
		}
		return new Object[0];
	}

	public Object getParent(Object obj) {
		if (obj instanceof IMakeTarget) {
			return ((IMakeTarget) obj).getContainer();
		} else if (obj instanceof IContainer) {
			return ((IContainer) obj).getParent();
		}
		return null;
	}

	public boolean hasChildren(Object obj) {
		return getChildren(obj).length > 0;
	}

	public Object[] getElements(Object obj) {
		return getChildren(obj);
	}

	public void dispose() {
		if (viewer != null) {
			MakeCorePlugin.getDefault().getTargetProvider().removeListener(this);
		}
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (this.viewer == null) {
			MakeCorePlugin.getDefault().getTargetProvider().addListener(this);
		}
		this.viewer = (TreeViewer) viewer;
		IWorkspace oldWorkspace = null;
		IWorkspace newWorkspace = null;
		if (oldInput instanceof IWorkspace) {
			oldWorkspace = (IWorkspace) oldInput;
		} else if (oldInput instanceof IContainer) {
			oldWorkspace = ((IContainer) oldInput).getWorkspace();
		}
		if (newInput instanceof IWorkspace) {
			newWorkspace = (IWorkspace) newInput;
		} else if (newInput instanceof IContainer) {
			newWorkspace = ((IContainer) newInput).getWorkspace();
		}
		if (oldWorkspace != newWorkspace) {
			if (oldWorkspace != null) {
				oldWorkspace.removeResourceChangeListener(this);
			}
			if (newWorkspace != null) {
				newWorkspace.addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
			}
		}
	}

	public void targetChanged(final MakeTargetEvent event) {
		final Control ctrl = viewer.getControl();
		if (ctrl != null && !ctrl.isDisposed()) {
			switch (event.getType()) {
				case MakeTargetEvent.PROJECT_ADDED :
				case MakeTargetEvent.PROJECT_REMOVED :
					ctrl.getDisplay().syncExec(new Runnable() {
						public void run() {
							if (ctrl != null && !ctrl.isDisposed()) {
								viewer.refresh();
							}
						}
					});
					break;
				case MakeTargetEvent.TARGET_ADD :
				case MakeTargetEvent.TARGET_CHANGED :
				case MakeTargetEvent.TARGET_REMOVED :
					ctrl.getDisplay().syncExec(new Runnable() {
						public void run() {
							if (ctrl != null && !ctrl.isDisposed()) {
								viewer.refresh(event.getTarget().getContainer());
							}
						}
					});
					break;
			}
		}
	}

	void processDelta(IResourceDelta delta) {
		// Bail out if the widget was disposed.
		Control ctrl = viewer.getControl();
		if (ctrl == null || ctrl.isDisposed() || delta == null) {
			return;
		}

		// Get the affected resource
		IResource resource = delta.getResource();
		IResourceDelta[] affectedChildren = delta.getAffectedChildren(IResourceDelta.CHANGED);

		// Not interested in Content changes.
		for (int i = 0; i < affectedChildren.length; i++) {
			if ((affectedChildren[i].getFlags() & IResourceDelta.TYPE) != 0) {
				return;
			}
		}

		//		Handle changed children recursively.
		for (int i = 0; i < affectedChildren.length; i++) {
			processDelta(affectedChildren[i]);
		}

		//		Handle removed children. Issue one update for all removals.
		affectedChildren = delta.getAffectedChildren(IResourceDelta.REMOVED);
		if (affectedChildren.length > 0) {
			ArrayList affected = new ArrayList(affectedChildren.length);
			for (int i = 0; i < affectedChildren.length; i++) {
				if (affectedChildren[i].getResource() instanceof IContainer) {
					affected.add(affectedChildren[i].getResource());
				}
			}
			if (affected.size() != 0) {
				if (viewer instanceof AbstractTreeViewer) {
					((AbstractTreeViewer) viewer).remove(affected.toArray());
				} else {
					((StructuredViewer) viewer).refresh(resource);
				}
			}
		}

		// Handle added children. Issue one update for all insertions.
		affectedChildren = delta.getAffectedChildren(IResourceDelta.ADDED);
		if (affectedChildren.length > 0) {
			ArrayList affected = new ArrayList(affectedChildren.length);
			for (int i = 0; i < affectedChildren.length; i++) {
				if (affectedChildren[i].getResource() instanceof IContainer) {
					affected.add(affectedChildren[i].getResource());
				}
			}
			if (affected.size() != 0) {
				if (viewer instanceof AbstractTreeViewer) {
					((AbstractTreeViewer) viewer).add(resource, affected.toArray());
				} else {
					((StructuredViewer) viewer).refresh(resource);
				}
			}
		}
	}

	public void resourceChanged(IResourceChangeEvent event) {
		final IResourceDelta delta = event.getDelta();
		Control ctrl = viewer.getControl();
		if (ctrl != null && !ctrl.isDisposed()) {
			// Do a sync exec, not an async exec, since the resource delta
			// must be traversed in this method.  It is destroyed
			// when this method returns.
			ctrl.getDisplay().syncExec(new Runnable() {
				public void run() {
					processDelta(delta);
				}
			});
		}
	}
}
