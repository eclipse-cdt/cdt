/*******************************************************************************
 * Copyright (c) 2011, 2012 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.filters;

import org.eclipse.cdt.internal.core.model.CElement;
import org.eclipse.cdt.internal.core.model.Enumeration;
import org.eclipse.cdt.internal.core.model.Structure;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Filters out anonymous structs and enums.
 */
public class AnonymousStructFilter extends ViewerFilter {
	/**
	 * Returns the result of this filter, when applied to the
	 * given inputs.
	 *
	 * @return Returns true if element should be included in filtered set
	 */
	@Override
	public boolean select(Viewer viewer, Object parent, Object element) {
		if (element instanceof Structure || element instanceof Enumeration) {
			return !((CElement) element).getElementName().isEmpty();
		}
		return true;
	}
}