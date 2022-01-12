/*******************************************************************************
 * Copyright (c) 2010 Eclipse CDT Project and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Patrick Hofer - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.filters;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Filter for forward declarations
 */
public class ForwardDeclarationFilter extends ViewerFilter {

	/*
	 * @see ViewerFilter
	 */
	@Override
	public boolean select(Viewer viewer, Object parent, Object element) {
		if (!(element instanceof ICElement))
			return true;

		final ICElement celem = (ICElement) element;
		ICElement tu = celem;
		while (tu != null && !(tu instanceof ITranslationUnit)) {
			tu = tu.getParent();
		}

		// Don't filter forward declarations in header file
		if (tu instanceof ITranslationUnit && ((ITranslationUnit) tu).isHeaderUnit())
			return true;

		switch (celem.getElementType()) {
		case ICElement.C_FUNCTION_DECLARATION:
		case ICElement.C_TEMPLATE_FUNCTION_DECLARATION:

		case ICElement.C_STRUCT_DECLARATION:
		case ICElement.C_UNION_DECLARATION:
		case ICElement.C_CLASS_DECLARATION:
		case ICElement.C_TEMPLATE_CLASS_DECLARATION:
		case ICElement.C_TEMPLATE_STRUCT_DECLARATION:
		case ICElement.C_TEMPLATE_UNION_DECLARATION:

		case ICElement.C_VARIABLE_DECLARATION:
			return false;
		}

		return true;
	}

}
