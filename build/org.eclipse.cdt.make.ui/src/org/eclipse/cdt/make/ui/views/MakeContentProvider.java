package org.eclipse.cdt.make.ui.views;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetListener;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeTargetEvent;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class MakeContentProvider implements ITreeContentProvider, IMakeTargetListener {
	protected Viewer viewer;

	/**
	 * Constructor for MakeContentProvider
	 */
	public MakeContentProvider() {
	}

	/**
	 * @see ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object obj) {
		if (obj instanceof IContainer) {
			try {
				return MakeCorePlugin.getDefault().getTargetProvider().getTargets((IContainer)obj);
			} catch (CoreException e) {
			}
		}
		return new Object[0];
	}

	/**
	 * @see ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object obj) {
		if (obj instanceof IMakeTarget) {
			return ((IMakeTarget)obj).getContainer();
		} else if (obj instanceof IContainer) {
			return ((IContainer)obj).getParent();
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
			MakeCorePlugin.getDefault().getTargetProvider().removeListener(this);
		}
	}

	/**
	 * @see IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (this.viewer == null) {
			MakeCorePlugin.getDefault().getTargetProvider().addListener(this);
		}
		this.viewer = viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.IMakeTargetListener#targetChanged(org.eclipse.cdt.make.core.MakeTargetEvent)
	 */
	public void targetChanged(MakeTargetEvent event) {
		
	}
}
