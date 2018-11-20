/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

/**
 * Provides common functionality for URI and IPath
 */
abstract class LocationAdapter<T> {

	public abstract String extractName(T location);

	public abstract IFile[] platformsFindFilesForLocation(T location);

	public abstract String getCanonicalPath(T location);

	public abstract T getLocation(IFile file);

	public static final LocationAdapter<IPath> PATH = new LocationAdapter<IPath>() {
		@Override
		public String extractName(IPath location) {
			String name = location.lastSegment();
			if (name != null)
				return name;
			return location.toString();
		}

		@Override
		public IFile[] platformsFindFilesForLocation(IPath location) {
			final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			return root.findFilesForLocationURI(URIUtil.toURI(location.makeAbsolute()));
		}

		@Override
		public String getCanonicalPath(IPath location) {
			final File file = location.toFile();
			try {
				return file.getCanonicalPath();
			} catch (IOException e) {
				// use non-canonical version
				return file.getAbsolutePath();
			}
		}

		@Override
		public IPath getLocation(IFile file) {
			return file.getLocation();
		}
	};

	public static final LocationAdapter<URI> URI = new LocationAdapter<URI>() {
		@Override
		public String extractName(URI location) {
			String path = location.getPath();
			int idx = path.lastIndexOf('/');
			return path.substring(idx + 1);
		}

		@Override
		public IFile[] platformsFindFilesForLocation(URI location) {
			return ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(location);
		}

		@Override
		public String getCanonicalPath(URI location) {
			if (!"file".equals(location.getScheme())) //$NON-NLS-1$
				return null;

			String path = location.getPath();
			try {
				return new File(path).getCanonicalPath();
			} catch (IOException e) {
				// use non-canonical version
				return path;
			}
		}

		@Override
		public URI getLocation(IFile file) {
			return file.getLocationURI();
		}
	};
}
