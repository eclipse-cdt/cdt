/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
package org.eclipse.cdt.ui.newui;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * see org.eclipse.cdt.internal.ui.dialogs.TypedViewerFilter
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class TypedCDTViewerFilter extends ViewerFilter {

	private Class<?>[] types;

	public TypedCDTViewerFilter(Class<?>[] _types) {
		types = _types;
	}

	/**
	 * @see ViewerFilter#select
	 */
	@Override
	public boolean select(Viewer viewer, Object parent, Object element) {
		for (Class<?> type : types) {
			if (type.isInstance(element))
				return true;
		}
		return false;
	}
}
