package org.eclipse.cdt.make.ui;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002. All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetListener;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeTargetEvent;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;

public class MakeContentProvider implements ITreeContentProvider, IMakeTargetListener {

	protected boolean bFlatten;

	protected StructuredViewer viewer;

	/**
	 * Constructor for MakeContentProvider
	 */
	public MakeContentProvider() {
		this(false);
	}

	public MakeContentProvider(boolean flat) {
		bFlatten = flat;
	}

	public Object[] getChildren(Object obj) {
		if (obj instanceof IWorkspaceRoot) {
			try {
				return MakeCorePlugin.getDefault().getTargetManager().getTargetBuilderProjects();
			} catch (CoreException e) {
				// ignore
			}
		} else if (obj instanceof IContainer) {
			ArrayList children = new ArrayList();
			try {
				IResource[] resource = ((IContainer)obj).members();
				for (int i = 0; i < resource.length; i++) {
					if (resource[i] instanceof IContainer) {
						children.add(resource[i]);
					}
				}
				children.addAll(Arrays.asList(MakeCorePlugin.getDefault().getTargetManager().getTargets((IContainer)obj)));
			} catch (CoreException e) {
				// ignore
			}
			return children.toArray();
		}
		return new Object[0];
	}

	public Object getParent(Object obj) {
		if (obj instanceof IMakeTarget) {
			return ((IMakeTarget)obj).getContainer();
		} else if (obj instanceof IContainer) {
			return ((IContainer)obj).getParent();
		}
		return null;
	}

	public boolean hasChildren(Object obj) {
		return getChildren(obj).length > 0;
	}

	public Object[] getElements(Object obj) {
		if (bFlatten) {
			List list = new ArrayList();
			Object[] children = getChildren(obj);
			for (int i = 0; i < children.length; i++) {
				list.add(children[i]);
				list.addAll(Arrays.asList(getElements(children[i])));
			}
			return list.toArray();
		}
		return getChildren(obj);
	}

	public void dispose() {
		if (viewer != null) {
			MakeCorePlugin.getDefault().getTargetManager().removeListener(this);
		}
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (this.viewer == null) {
			MakeCorePlugin.getDefault().getTargetManager().addListener(this);
		}
		this.viewer = (StructuredViewer)viewer;
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
								if (bFlatten) {
									viewer.refresh();
								} else {
									viewer.refresh(event.getTarget().getContainer());
								}
							}
						}
					});
					break;
			}
		}
	}
}