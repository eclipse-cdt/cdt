/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
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
	protected int fKind;
	protected boolean fExportedOnly;

	/**
	 * @param excludedFiles
	 *            Excluded paths will not pass the filter. <code>null</code> is allowed if no files should be excluded.
	 * @param recusive
	 *            Folders are only shown if, searched recursivly, contain an archive
	 */
	public CPElementFilter(Object[] excludedElements, int kind, boolean exportedOnly) {
		if (excludedElements != null) {
			fExcludes = Arrays.asList(excludedElements);
		}
		fKind = kind;
		fExportedOnly = exportedOnly;
	}

	public CPElementFilter(int kind, boolean exportedOnly) {
		this(null, kind, exportedOnly);
	}

	/*
	 * @see ViewerFilter#select
	 */
	public boolean select(Viewer viewer, Object parent, Object element) {
		if (element instanceof CPElement) {
			if ( ((CPElement)element).getEntryKind() == fKind) {
				if (fExcludes == null || !fExcludes.contains(element)) {
					if (fExportedOnly == true) {
						return ((CPElement)element).isExported();
					}
					return true;
				}
			}
		} else if (element instanceof IPathEntry) {
			if ( ((IPathEntry)element).getEntryKind() == fKind) {
				if (fExcludes == null || !fExcludes.contains(element)) {
					if (fExportedOnly == true) {
						return ((IPathEntry)element).isExported();
					}
				}
			}
		}
		return false;
	}
}