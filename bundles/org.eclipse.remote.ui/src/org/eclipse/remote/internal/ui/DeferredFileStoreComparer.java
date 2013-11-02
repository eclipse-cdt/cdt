/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.ui;

import org.eclipse.jface.viewers.IElementComparer;

public class DeferredFileStoreComparer implements IElementComparer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IElementComparer#equals(java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean equals(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null) {
			return false; // o2 != null if we reach this point
		}
		if (o1.equals(o2)) {
			return true;
		}

		// Assume they are DeferredFileStore
		DeferredFileStore c1 = (o1 instanceof DeferredFileStore) ? (DeferredFileStore) o1 : null;
		DeferredFileStore c2 = (o2 instanceof DeferredFileStore) ? (DeferredFileStore) o2 : null;
		if (c1 == null || c2 == null) {
			return false;
		}
		return c1.getFileStore().equals(c2.getFileStore());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IElementComparer#hashCode(java.lang.Object)
	 */
	@Override
	public int hashCode(Object element) {
		if (element instanceof DeferredFileStore) {
			return ((DeferredFileStore) element).getFileStore().hashCode();
		}
		return element.hashCode();
	}
}
