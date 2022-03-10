/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
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

import org.eclipse.jface.viewers.IElementComparer;

public class DeferredFileStoreComparer implements IElementComparer {

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

	@Override
	public int hashCode(Object element) {
		if (element instanceof DeferredFileStore) {
			return ((DeferredFileStore) element).getFileStore().hashCode();
		}
		return element.hashCode();
	}
}
