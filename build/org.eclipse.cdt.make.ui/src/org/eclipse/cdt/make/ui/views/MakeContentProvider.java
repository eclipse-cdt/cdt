package org.eclipse.cdt.make.ui.views;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetListener;
import org.eclipse.cdt.make.core.IMakeTargetProvider;
import org.eclipse.cdt.make.core.MakeTargetEvent;
import org.eclipse.core.resources.IContainer;
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
			if (viewer != null) {
				Object input = viewer.getInput();
				if (input instanceof IMakeTargetProvider) {
					IMakeTargetProvider provider = (IMakeTargetProvider)obj;
					return provider.getTargets((IContainer)obj);
				}
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
			Object obj = viewer.getInput();
			if (obj instanceof IMakeTargetProvider) {
				IMakeTargetProvider provider = (IMakeTargetProvider)obj;
				provider.removeListener(this);
				provider = null;
			}
		}
	}

	/**
	 * @see IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
		if (oldInput != null) {
			if (oldInput instanceof IMakeTargetProvider) {
				((IMakeTargetProvider)oldInput).removeListener(this);
			}
		}
		if (newInput != null) {
			if (newInput instanceof IMakeTargetProvider) {
				((IMakeTargetProvider)newInput).addListener(this);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.IMakeTargetListener#targetChanged(org.eclipse.cdt.make.core.MakeTargetEvent)
	 */
	public void targetChanged(MakeTargetEvent event) {
		
	}
}
