/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

public class CPElementSorter extends ViewerSorter {

	private static final int SOURCE = 0;
	private static final int PROJECT = 1;
	private static final int LIBRARY = 2;
	private static final int CONTAINER = 3;
	private static final int OTHER = 5;

	/*
	 * @see ViewerSorter#category(Object)
	 */
	public int category(Object obj) {
		if (obj instanceof CPElement) {
			switch ( ((CPElement)obj).getEntryKind()) {
				case IPathEntry.CDT_LIBRARY :
					return LIBRARY;
				case IPathEntry.CDT_PROJECT :
					return PROJECT;
				case IPathEntry.CDT_SOURCE :
					return SOURCE;
				case IPathEntry.CDT_CONTAINER :
					return CONTAINER;
			}
		} else if (obj instanceof CPElementGroup) {
			switch ( ((CPElementGroup)obj).getEntryKind()) {
				case IPathEntry.CDT_LIBRARY :
					return LIBRARY;
				case IPathEntry.CDT_PROJECT :
					return PROJECT;
				case IPathEntry.CDT_SOURCE :
					return SOURCE;
				case IPathEntry.CDT_CONTAINER :
					return CONTAINER;
				case -1 :
					if ( ((CPElementGroup)obj).getResource() instanceof IProject) {
						return PROJECT;
					}
			}
		}
		return OTHER;
	}

	public void sort(Viewer viewer, Object[] elements) {
		// include paths and symbol definitions must not be sorted
		List sort = new ArrayList(elements.length);
		List includes = new ArrayList(elements.length);
		List syms = new ArrayList(elements.length);
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] instanceof CPElement) {
				CPElement element = (CPElement)elements[i];
				if (element.getEntryKind() == IPathEntry.CDT_INCLUDE) {
					includes.add(elements[i]);
				} else if (element.getEntryKind() == IPathEntry.CDT_MACRO) {
					syms.add(elements[i]);
				} else {
					sort.add(elements[i]);
				}
			} else {
				sort.add(elements[i]);
			}
		}
		System.arraycopy(sort.toArray(), 0, elements, 0, sort.size());
		super.sort(viewer, elements);
		System.arraycopy(includes.toArray(), 0, elements, sort.size(), includes.size());
		System.arraycopy(syms.toArray(), 0, elements, sort.size() + includes.size(), syms.size());
	}

}
