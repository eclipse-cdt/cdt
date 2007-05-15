/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 *******************************************************************************/
package org.eclipse.rse.internal.importexport;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rse.internal.importexport.files.RemoteFileExportActionDelegate;
import org.eclipse.rse.ui.SystemBasePlugin;

/**
 * Utility class for import and export. A singleton class.
 */
public class RemoteImportExportUtil {
	private static RemoteImportExportUtil instance;

	/**
	 * Dummy action. Does nothing.
	 */
	private class DummyAction extends Action {
		/**
		 * Constructor.
		 */
		public DummyAction() {
			super();
		}
	}

	private RemoteImportExportUtil() {
	}

	public static RemoteImportExportUtil getInstance() {
		if (instance == null) {
			instance = new RemoteImportExportUtil();
		}
		return instance;
	}

	/**
	 * Does an export from a description file. The given description file must exist.
	 * @param descriptionFile the export description file.
	 */
	public void exportFromDescriptionFile(IFile descriptionFile) {
		Assert.isLegal((descriptionFile != null) && descriptionFile.exists());
		IFile file = descriptionFile;
		String extension = file.getFileExtension();
		if (extension == null || extension.equals("")) { //$NON-NLS-1$
			return;
		}
		if (extension.equals(IRemoteImportExportConstants.REMOTE_FILE_EXPORT_DESCRIPTION_FILE_EXTENSION)) {
			RemoteFileExportActionDelegate action = new RemoteFileExportActionDelegate();
			DummyAction dummy = new DummyAction();
			action.selectionChanged(dummy, new StructuredSelection(file));
			action.run(dummy);
		} else if (extension.equals(IRemoteImportExportConstants.REMOTE_JAR_EXPORT_DESCRIPTION_FILE_EXTENSION)) {
			// TODO
			// CreateRemoteJarActionDelegate action = new CreateRemoteJarActionDelegate();
			// DummyAction dummy = new DummyAction();
			// action.selectionChanged(dummy, new StructuredSelection(file));
			// action.run(dummy);
		}
	}

	/**
	 * Helper method for case insensitive file systems.  Returns
	 * an existing resource whose path differs only in case from
	 * the given path, or null if no such resource exists.
	 */
	public IResource findExistingResourceVariant(IPath target) {
		IWorkspace workspace = SystemBasePlugin.getWorkspace();
		// check if local file system is case sensitive
		boolean isCaseInsensitive = Platform.getOS().equals(Platform.OS_WIN32);
		// if so, we don't need to go any further
		if (!isCaseInsensitive) {
			return null;
		}
		IWorkspaceRoot root = workspace.getRoot();
		IPath result = root.getFullPath();
		IContainer container = root;
		int segmentCount = target.segmentCount();
		for (int i = 0; i < segmentCount; i++) {
			IResource[] children = null;
			if (i != 0) {
				IResource resource = root.findMember(result);
				if ((resource != null) && (resource instanceof IContainer)) {
					container = (IContainer) resource;
				} else {
					return null;
				}
			}
			try {
				children = container.members();
			} catch (CoreException e) {
				SystemBasePlugin.logError("Exception occured trying to get children of " + result, e); //$NON-NLS-1$
			}
			String name = findVariant(target.segment(i), children);
			if (name == null) {
				return null;
			}
			result = result.append(name);
		}
		return root.findMember(result);
	}

	/**
	 * Searches for a variant of the given target in the list,
	 * that differs only in case. Returns the variant from
	 * the list if one is found, otherwise returns null.
	 * @param target the name.
	 * @param list the list of resources that may have the variant
	 */
	private String findVariant(String target, IResource[] list) {
		if (list == null) {
			return null;
		}
		// go through list
		for (int i = 0; i < list.length; i++) {
			String name = list[i].getName();
			// see if there is a variant, and if so, return it
			if (target.equalsIgnoreCase(name)) {
				return name;
			}
		}
		return null;
	}
}
