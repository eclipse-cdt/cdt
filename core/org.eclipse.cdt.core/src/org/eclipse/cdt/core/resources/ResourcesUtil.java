/*******************************************************************************
 * Copyright (c) 2010, 2010 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.resources;

import java.net.URI;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

/**
 * A collection of utility methods related to resources.
 *
 * @since 5.3
 */
public class ResourcesUtil {
	/**
	 * Refresh file when it happens to belong to Workspace. There could
	 * be multiple workspace {@link IFile} associated with one URI.
	 * Hint: use {@link org.eclipse.core.filesystem.URIUtil#toURI(String)}
	 * to convert filesystem path to URI.
	 *
	 * @param uri - URI of the file.
	 */
	public static void refreshWorkspaceFiles(URI uri) {
		if (uri != null) {
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
			for (IFile file : files) {
				try {
					file.refreshLocal(IResource.DEPTH_ZERO, null);
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
			}
		}
	}

}
