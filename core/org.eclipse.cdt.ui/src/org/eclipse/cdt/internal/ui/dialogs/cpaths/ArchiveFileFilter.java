/*******************************************************************************
 * Copyright (c) 2002, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Viewer filter for archive selection dialogs.
 * Archives are files with file extension "a",  "dll", "so.
 * The filter is not case sensitive.
 *
 * @deprecated as of CDT 4.0. This class was used for property pages
 * for 3.X style projects.
 */
@Deprecated
public class ArchiveFileFilter extends ViewerFilter {

	private static final String[] fgArchiveExtensions = { "a", "so", "dll" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	private List<IFile> fExcludes;
	private boolean fRecursive;

	/**
	 * @param excludedFiles Excluded files will not pass the filter.
	 * <code>null</code> is allowed if no files should be excluded.
	 * @param recusive Folders are only shown if, searched recursivly, contain
	 * an archive
	 */
	public ArchiveFileFilter(IFile[] excludedFiles, boolean recusive) {
		if (excludedFiles != null) {
			fExcludes = Arrays.asList(excludedFiles);
		} else {
			fExcludes = null;
		}
		fRecursive = recusive;
	}

	/*
	 * @see ViewerFilter#select
	 */
	@Override
	public boolean select(Viewer viewer, Object parent, Object element) {
		if (element instanceof IFile) {
			if (fExcludes != null && fExcludes.contains(element)) {
				return false;
			}
			return isArchivePath(((IFile) element).getFullPath());
		} else if (element instanceof IContainer) { // IProject, IFolder
			if (!fRecursive) {
				return true;
			}
			try {
				IResource[] resources = ((IContainer) element).members();
				for (int i = 0; i < resources.length; i++) {
					// recursive! Only show containers that contain an archive
					if (select(viewer, parent, resources[i])) {
						return true;
					}
				}
			} catch (CoreException e) {
				CUIPlugin.log(e.getStatus());
			}
		}
		return false;
	}

	public static boolean isArchivePath(IPath path) {
		String ext = path.getFileExtension();
		if (ext != null && ext.length() != 0) {
			return isArchiveFileExtension(ext);
		}
		return false;
	}

	public static boolean isArchiveFileExtension(String ext) {
		for (int i = 0; i < fgArchiveExtensions.length; i++) {
			if (ext.equalsIgnoreCase(fgArchiveExtensions[i])) {
				return true;
			}
		}
		return false;
	}

}
