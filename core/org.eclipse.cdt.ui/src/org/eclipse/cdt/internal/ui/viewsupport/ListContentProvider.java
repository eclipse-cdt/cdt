/*******************************************************************************
 *  Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * A specialized content provider to show a list of editor parts.
 */
public class ListContentProvider implements IStructuredContentProvider {
	List<?> fContents;

	public ListContentProvider() {
	}

	@Override
	public Object[] getElements(Object input) {
		if (fContents != null && fContents == input)
			return fContents.toArray();
		return new Object[0];
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof List<?>)
			fContents = (List<?>) newInput;
		else
			fContents = null;
		// we use a fixed set.
	}

	@Override
	public void dispose() {
	}

	public boolean isDeleted(Object o) {
		return fContents != null && !fContents.contains(o);
	}
}
