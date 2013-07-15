/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.internal.remote.ui;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.progress.DeferredTreeContentManager;

/**
 * Extension to the generic workbench content provider mechanism
 * to lazily determine whether an element has children.  That is,
 * children for an element aren't fetched until the user clicks
 * on the tree expansion box.
 */
public class RemoteContentProvider extends WorkbenchContentProvider {
	private IWorkingSet workingSet;
	private DeferredTreeContentManager manager;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput != null) {
			manager = new DeferredTreeContentManager((AbstractTreeViewer) viewer);
		} else {
			manager = null;
		}
		super.inputChanged(viewer, oldInput, newInput);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.BaseWorkbenchContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		if (manager != null && manager.isDeferredAdapter(element)) {
			return manager.mayHaveChildren(element);
		}

		return super.hasChildren(element);
	}

	/**
	 * Sets the workingSet.
	 * @param workingSet The workingSet to set
	 */
	public void setWorkingSet(IWorkingSet workingSet) {
		this.workingSet = workingSet;
	}

	/**
	 * Returns the workingSet.
	 * @return IWorkingSet
	 */
	public IWorkingSet getWorkingSet() {
		return workingSet;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.WorkbenchContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object element) {
		if (manager != null) {
			Object[] children = manager.getChildren(element);
			if (children != null) {
				return children;
			}
		}
		return super.getChildren(element);
	}
}
