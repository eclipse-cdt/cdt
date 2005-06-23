/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.scannerconfig;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * TODO Provide description
 * 
 * @author vhirsl
 */
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
