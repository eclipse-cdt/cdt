/*******************************************************************************
 * Copyright (c) 2001, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Rational Software - initial implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IDeclaration;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.model.IMember;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Filter for the methods viewer.
 * Changing a filter property does not trigger a refiltering of the viewer
 */

public class MemberFilter extends ViewerFilter {

	public static final int FILTER_NONPUBLIC = 1;
	public static final int FILTER_STATIC = 2;
	public static final int FILTER_FIELDS = 4;
	public static final int FILTER_INACTIVE = 0x10;

	/** @deprecated Unsupported filter constant */
	@Deprecated
	public static final int FILTER_LOCALTYPES = 8;

	private int fFilterProperties;

	/**
	 * Modifies filter and add a property to filter for
	 */
	public final void addFilter(int filter) {
		fFilterProperties |= filter;
	}

	/**
	 * Modifies filter and remove a property to filter for
	 */
	public final void removeFilter(int filter) {
		fFilterProperties &= (-1 ^ filter);
	}

	/**
	 * Tests if a property is filtered
	 */
	public final boolean hasFilter(int filter) {
		return (fFilterProperties & filter) != 0;
	}

	/*
	 * @see ViewerFilter@isFilterProperty
	 */
	public boolean isFilterProperty(Object element, Object property) {
		return false;
	}

	/*
	 * @see ViewerFilter@select
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof IDeclaration) {
			try {
				IDeclaration declaration = (IDeclaration) element;
				if (hasFilter(FILTER_STATIC) && (declaration.isStatic())) {
					return false;
				}
				if (element instanceof IMember) {
					IMember member = (IMember) element;
					if (hasFilter(FILTER_NONPUBLIC) && (member.getVisibility() != ASTAccessVisibility.PUBLIC)) {
						return false;
					}

					if (hasFilter(FILTER_FIELDS) && element instanceof IField) {
						return false;
					}
				}
			} catch (CModelException e) {
				// ignore
			}
		}
		if (hasFilter(FILTER_INACTIVE)) {
			if (element instanceof ISourceReference && !((ISourceReference) element).isActive()) {
				return false;
			}
		}
		return true;
	}
}
