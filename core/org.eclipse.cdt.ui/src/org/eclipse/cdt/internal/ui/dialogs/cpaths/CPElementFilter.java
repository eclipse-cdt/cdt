/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Viewer filter for archive selection dialogs. Archives are files with file extension '.so', '.dll' and '.a'. The filter is not
 * case sensitive.
 */
public class CPElementFilter extends ViewerFilter {

	protected List fExcludes;
	protected int[] fKind;
	protected boolean fExportedOnly;
	protected boolean fShowInherited;

	/**
	 * @param excludedElements
	 *            Excluded paths will not pass the filter. <code>null</code> is allowed if no files should be excluded.
	 */
	public CPElementFilter(Object[] excludedElements, int[] kind, boolean exportedOnly, boolean showInherited) {
		if (excludedElements != null) {
			fExcludes = Arrays.asList(excludedElements);
		}
		fKind = kind;
		fExportedOnly = exportedOnly;
		fShowInherited = showInherited;
	}

	public CPElementFilter(int[] kind, boolean exportedOnly, boolean showInherited) {
		this(null, kind, exportedOnly, showInherited);
	}

	/*
	 * @see ViewerFilter#select
	 */
	public boolean select(Viewer viewer, Object parent, Object element) {
		if (element instanceof CPElement) {
			for (int i = 0; i < fKind.length; i++) {
				if ( ((CPElement)element).getEntryKind() == fKind[i]) {
					if (fExcludes == null || !fExcludes.contains(element)) {
						if (fExportedOnly == true) {
							if ( !fShowInherited ) {
								return ((CPElement)element).getInherited() == null && ((CPElement)element).isExported();
							}
							return ((CPElement)element).isExported();
						}
						if ( !fShowInherited ) {
							return ((CPElement)element).getInherited() == null;
						}
						return true;
					}
				}
			}
		} else if (element instanceof IPathEntry) {
			for (int i = 0; i < fKind.length; i++) {
				if ( ((IPathEntry)element).getEntryKind() == fKind[i]) {
					if (fExcludes == null || !fExcludes.contains(element)) {
						if (fExportedOnly == true) {
							return ((IPathEntry)element).isExported();
						}
						return true;
					}
				}
			}
		} else if (element instanceof CPElementGroup) {
			for (int i = 0; i < fKind.length; i++) {
				if ( ((CPElementGroup)element).getEntryKind() == fKind[i]) {
					return true;
				}
			}
		} else {
			return true;
		}
		return false;
	}
}
