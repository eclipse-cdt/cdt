/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.scannerconfig;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * @deprecated as of CDT 4.0. This class was used to set preferences/properties
 * for 3.X style projects.
 *
 * @author vhirsl
 */
@Deprecated
public class DiscoveredElementSorter extends ViewerSorter {
	private static final int CONTAINER = 0;
	private static final int PATHS_GROUP = 1;
	private static final int SYMBOLS_GROUP = 2;
	private static final int INCLUDE_FILE_GROUP = 3;
	private static final int MACROS_FILE_GROUP = 4;
	private static final int OTHER = 10;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerSorter#category(java.lang.Object)
	 */
	@Override
	public int category(Object element) {
		if (element instanceof DiscoveredElement) {
			DiscoveredElement elem = (DiscoveredElement) element;
			switch (elem.getEntryKind()) {
			case DiscoveredElement.CONTAINER:
				return CONTAINER;
			case DiscoveredElement.PATHS_GROUP:
				return PATHS_GROUP;
			case DiscoveredElement.SYMBOLS_GROUP:
				return SYMBOLS_GROUP;
			case DiscoveredElement.INCLUDE_FILE_GROUP:
				return INCLUDE_FILE_GROUP;
			case DiscoveredElement.MACROS_FILE_GROUP:
				return MACROS_FILE_GROUP;
			}
		}
		return OTHER;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerSorter#sort(org.eclipse.jface.viewers.Viewer, java.lang.Object[])
	 */
	@Override
	public void sort(Viewer viewer, Object[] elements) {
		if (elements.length > 0 && elements[0] instanceof DiscoveredElement) {
			DiscoveredElement firstElem = (DiscoveredElement) elements[0];
			switch (firstElem.getEntryKind()) {
			case DiscoveredElement.INCLUDE_PATH:
			case DiscoveredElement.SYMBOL_DEFINITION:
			case DiscoveredElement.INCLUDE_FILE:
			case DiscoveredElement.MACROS_FILE:
				return;
			}
		}
		super.sort(viewer, elements);
	}
}
