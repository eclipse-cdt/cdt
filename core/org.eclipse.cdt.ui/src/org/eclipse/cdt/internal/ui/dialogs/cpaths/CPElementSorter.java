/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * @deprecated as of CDT 4.0. This class was used for property pages
 * for 3.X style projects.
 */
@Deprecated
public class CPElementSorter extends ViewerSorter {

	private static final int SOURCE = 0;
	private static final int PROJECT = 1;
	private static final int LIBRARY = 2;
	private static final int CONTAINER = 3;
	private static final int OTHER = 5;

	/*
	 * @see ViewerSorter#category(Object)
	 */
	@Override
	public int category(Object obj) {
		if (obj instanceof CPElement) {
			switch (((CPElement) obj).getEntryKind()) {
			case IPathEntry.CDT_LIBRARY:
				return LIBRARY;
			case IPathEntry.CDT_PROJECT:
				return PROJECT;
			case IPathEntry.CDT_SOURCE:
				return SOURCE;
			case IPathEntry.CDT_CONTAINER:
				return CONTAINER;
			}
		} else if (obj instanceof CPElementGroup) {
			switch (((CPElementGroup) obj).getEntryKind()) {
			case IPathEntry.CDT_LIBRARY:
				return LIBRARY;
			case IPathEntry.CDT_PROJECT:
				return PROJECT;
			case IPathEntry.CDT_SOURCE:
				return SOURCE;
			case IPathEntry.CDT_CONTAINER:
				return CONTAINER;
			case -1:
				if (((CPElementGroup) obj).getResource() instanceof IProject) {
					return PROJECT;
				}
			}
		}
		return OTHER;
	}

	@Override
	public void sort(Viewer viewer, Object[] elements) {
		// include paths and symbol definitions must not be sorted
		List<Object> sort = new ArrayList<>(elements.length);
		List<CPElement> includes = new ArrayList<>(elements.length);
		List<CPElement> syms = new ArrayList<>(elements.length);
		for (Object element : elements) {
			if (element instanceof CPElement) {
				CPElement cpelement = (CPElement) element;
				if (cpelement.getEntryKind() == IPathEntry.CDT_INCLUDE) {
					includes.add(cpelement);
				} else if (cpelement.getEntryKind() == IPathEntry.CDT_MACRO) {
					syms.add(cpelement);
				} else {
					sort.add(cpelement);
				}
			} else {
				sort.add(element);
			}
		}
		System.arraycopy(sort.toArray(), 0, elements, 0, sort.size());
		super.sort(viewer, elements);
		System.arraycopy(includes.toArray(), 0, elements, sort.size(), includes.size());
		System.arraycopy(syms.toArray(), 0, elements, sort.size() + includes.size(), syms.size());
	}

}
