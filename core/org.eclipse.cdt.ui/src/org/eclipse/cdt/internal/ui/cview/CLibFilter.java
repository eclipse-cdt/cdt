package org.eclipse.cdt.internal.ui.cview;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import org.eclipse.cdt.core.model.ILibraryReference;

public class CLibFilter extends ViewerFilter {
        private boolean showlib = true;

	public boolean getShowLibraries() {
		return showlib;
	}

	/* (non-Javadoc)
	 * Method declared on ViewerFilter.
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof ILibraryReference) {
			return showlib;
		}
		return true;
	}

	public void setShowLibraries (boolean show) {
		showlib = show;
	}

	/**
	* Creates a new library filter.
	*/
	public CLibFilter() {
		super();
	}
}
