/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.resources;

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
			return PathCanonicalizationStrategy.getCanonicalPath(location.toFile());
		}

		@Override
		public IPath getLocation(IFile file) {
			return file.getLocation();
		}
	};

	public static final LocationAdapter<URI> URI = new LocationAdapter<URI>() {
		@Override
		public String extractName(URI location) {
			String path= location.getPath();
			int idx= path.lastIndexOf('/');
			return path.substring(idx + 1);
		}

		@Override
		public IFile[] platformsFindFilesForLocation(URI location) {
			return ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(location);
		}

		@Override
		public String getCanonicalPath(URI location) {
			IPath path = URIUtil.toPath(location);
			if (path == null) {
				return null;
			}
			return PathCanonicalizationStrategy.getCanonicalPath(path.toFile());
		}

		@Override
		public URI getLocation(IFile file) {
			return file.getLocationURI();
		}
	};
}
