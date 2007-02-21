/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * see org.eclipse.cdt.internal.ui.dialogs.TypedViewerFilter
 */
public class TypedCDTViewerFilter extends ViewerFilter {

	private Class[] types;

	public TypedCDTViewerFilter(Class[] _types) { types= _types; }
	/**
	 * @see ViewerFilter#select
	 */
	public boolean select(Viewer viewer, Object parent, Object element) {
		for (int i= 0; i < types.length; i++) {
			if (types[i].isInstance(element)) return true;
		}
		return false;
	}
}
