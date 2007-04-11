/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.importexport;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * This class defines a viewer filter that can be used with a selection dialog, e.g. <code>ElementTreeSelectionDialog</code>.
 * The viewer filter only allows description files for import/export. Current known extensions are defined
 * in class <code>RemoteFileImportExportConstants</code>.
 */
public class RemoteImportExportDescriptionFilesViewerFilter extends ViewerFilter {
	/**
	 * Constant indicating descriptor files for both import and export should be allowed.
	 */
	public static final int IMPORT_EXPORT = 0;
	/**
	 * Constant indicating that only descriptor files for imports should be allowed.
	 */
	public static final int IMPORT_ONLY = 1;
	/**
	 * Constant indicating that only descriptor files for exports should be allowed.
	 */
	public static final int EXPORT_ONLY = 2;
	private int mode;

	/**
	 * Constructor.
	 * @param mode the mode. One of <code>IMPORT_EXPORT</code>, <code>IMPORT_ONLY</code>, or <code>EXPORT_ONLY</code>.
	 */
	public RemoteImportExportDescriptionFilesViewerFilter(int mode) {
		Assert.isLegal((mode == IMPORT_EXPORT) || (mode == IMPORT_ONLY) || (mode == EXPORT_ONLY));
		this.mode = mode;
	}

	/**
	 * Allows containers and import/export description files.
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof IResource) {
			IResource resource = (IResource) element;
			if (resource.getType() == IResource.FILE) {
				String extension = resource.getFileExtension();
				if (extension == null || extension.equals("")) { //$NON-NLS-1$
					return false;
				}
				switch (mode) {
					case IMPORT_EXPORT:
						return (contains(IRemoteImportExportConstants.REMOTE_IMPORT_DESCRIPTION_FILE_EXTENSIONS, extension) || contains(
								IRemoteImportExportConstants.REMOTE_EXPORT_DESCRIPTION_FILE_EXTENSIONS, extension));
					case IMPORT_ONLY:
						return contains(IRemoteImportExportConstants.REMOTE_IMPORT_DESCRIPTION_FILE_EXTENSIONS, extension);
					case EXPORT_ONLY:
						return contains(IRemoteImportExportConstants.REMOTE_EXPORT_DESCRIPTION_FILE_EXTENSIONS, extension);
						// should never be here
					default:
						return false;
				}
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	/**
	 * Returns whether an extension exists in an array of extensions.
	 * @param extensions an array to extensions.
	 * @param extension the extension.
	 * @return <code>true</code> if the extension exists in the array of extensions, <code>false</code> otherwise.
	 */
	private boolean contains(String[] extensions, String extension) {
		for (int i = 0; i < extensions.length; i++) {
			if (extensions[i].equals(extension)) {
				return true;
			}
		}
		return false;
	}
}
