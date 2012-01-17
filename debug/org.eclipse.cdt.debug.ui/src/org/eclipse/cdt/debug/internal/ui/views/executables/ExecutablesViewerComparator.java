/*******************************************************************************
 * Copyright (c) 2008 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.executables;

import java.io.File;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.debug.core.executables.Executable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

class ExecutablesViewerComparator extends ViewerComparator {

	private int sortType;
	private int columnOrder;

	public ExecutablesViewerComparator(int sortType, int columnOrder) {
		this.sortType = sortType;
		this.columnOrder = columnOrder;
	}

	@Override
	public int category(Object element) {
		if (element instanceof ITranslationUnit || element instanceof Executable)
			return 1;
		return 0;
	}

	@Override
	@SuppressWarnings("unchecked")
	public int compare(Viewer viewer, Object e1, Object e2) {

		if (category(e1) == 1 && category(e2) == 1) {
			if (sortType == ExecutablesView.NAME) {
				String s1 = ((ColumnLabelProvider) ((TreeViewer) viewer).getLabelProvider()).getText(e1);
				String s2 = ((ColumnLabelProvider) ((TreeViewer) viewer).getLabelProvider()).getText(e2);
				return getComparator().compare(s1, s2) * columnOrder;
			}

			if (sortType == ExecutablesView.SIZE) {
				long file1 = getFileSize(e1);
				long file2 = getFileSize(e2);
				return Long.valueOf(file1).compareTo(Long.valueOf(file2)) * columnOrder;
			}

			if (sortType == ExecutablesView.LOCATION) {
				return getComparator().compare(getPath(e1).toOSString(), getPath(e2).toOSString()) * columnOrder;
			}

			if (sortType == ExecutablesView.MODIFIED) {
				long file1 = getPath(e1).toFile().lastModified();
				long file2 = getPath(e2).toFile().lastModified();
				return Long.valueOf(file1).compareTo(Long.valueOf(file2)) * columnOrder;
			}

			if (sortType == ExecutablesView.TYPE) {
				String ext1 = getPath(e1).getFileExtension();
				String s1 = ext1 != null ? ext1.toUpperCase() : ""; //$NON-NLS-1$
				String ext2 = getPath(e2).getFileExtension();
				String s2 = ext2 != null ? ext2.toUpperCase() : ""; //$NON-NLS-1$
				return getComparator().compare(s1, s2) * columnOrder;
			}
		}

		return super.compare(viewer, e1, e2);
	}

	private long getFileSize(Object element) {
		File file1 = getPath(element).toFile();
		if (file1.exists())
			return file1.length();
		return 0;
	}

	private IPath getPath(Object element) {
		if (element instanceof ITranslationUnit) {
			return ((ITranslationUnit) element).getPath();
		} else if (element instanceof Executable) {
			return ((Executable) element).getPath();
		}
		return null;
	}

}