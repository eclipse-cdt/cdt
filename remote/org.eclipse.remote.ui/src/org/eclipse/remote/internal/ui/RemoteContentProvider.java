/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.ui;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.model.WorkbenchContentProvider;

/**
 * Extension to the generic workbench content provider mechanism
 * to lazily determine whether an element has children. That is,
 * children for an element aren't fetched until the user clicks
 * on the tree expansion box.
 */
public class RemoteContentProvider extends WorkbenchContentProvider {
	private IWorkingSet workingSet;
	private RemoteTreeContentManager manager;

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput != null) {
			manager = new RemoteTreeContentManager(this, (RemoteTreeViewer) viewer, null);
		} else {
			manager = null;
		}
		super.inputChanged(viewer, oldInput, newInput);
	}

	@Override
	public boolean hasChildren(Object element) {
		if (manager != null /* && manager.isDeferredAdapter(element) */) {
			return manager.mayHaveChildren(element);
		}

		return super.hasChildren(element);
	}

	/**
	 * Sets the workingSet.
	 *
	 * @param workingSet
	 *            The workingSet to set
	 */
	public void setWorkingSet(IWorkingSet workingSet) {
		this.workingSet = workingSet;
	}

	/**
	 * Returns the workingSet.
	 *
	 * @return IWorkingSet
	 */
	public IWorkingSet getWorkingSet() {
		return workingSet;
	}

	@Override
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
