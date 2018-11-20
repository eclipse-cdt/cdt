/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
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
package org.eclipse.cdt.internal.ui.util;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * The PendingUpdateAdapter is a convenience object that can be used
 * by a BaseWorkbenchContentProvider that wants to show a pending update.
 */
public class PendingUpdateAdapter implements IWorkbenchAdapter, IAdaptable {

	boolean removed = false;

	/**
	 * Return whether or not this has been removed from the tree.
	 * @return boolean
	 */
	public boolean isRemoved() {
		return removed;
	}

	/**
	 * Set whether or not this has been removed from the tree.
	 * @param removedValue boolean
	 */
	public void setRemoved(boolean removedValue) {
		this.removed = removedValue;
	}

	/**
	 * Create a new instance of the receiver.
	 */
	public PendingUpdateAdapter() {
		//No initial behavior
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IWorkbenchAdapter.class)
			return (T) this;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object o) {
		return new Object[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	@Override
	public String getLabel(Object o) {
		return "Pending"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object o) {
		return null;
	}
}
