/*******************************************************************************
 * Copyright (c) 2006, 2015 Symbian Software Ltd. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.index;

import java.net.URI;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.index.IndexFileLocation;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.cdt.utils.UNCPathConverter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Factory for obtaining instances of IIndexFileLocation for workspace and external files, and
 * some utility methods for going in the opposite direction.
 *
 * @since 4.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class IndexLocationFactory {
	/**
	 * Returns
	 * <ul>
	 * <li> the full path if this IIndexFileLocation is within the workspace root
	 * <li> the absolute path if this IIndexFileLocation is URI based and corresponds
	 * to a location on the local file system
	 * <li> otherwise, null
	 * </ul>
	 * @param location
	 * @return the workspace root relative path, a local file system absolute path or null
	 */
	public static IPath getPath(IIndexFileLocation location) {
		String fp = location.getFullPath();
		if (fp != null) {
			return new Path(fp);
		}
		return getAbsolutePath(location);
	}

	/**
	 * Returns the absolute file path of a location, or {@code null}
	 * if the location is not a file-system path.
	 */
	public static IPath getAbsolutePath(IIndexFileLocation location) {
		return UNCPathConverter.toPath(location.getURI());
	}

	/**
	 * Equivalent to the overloaded form with the ICProject parameter set to null
	 * @see IndexLocationFactory#getIFLExpensive(ICProject, String)
	 */
	public static IIndexFileLocation getIFLExpensive(String absolutePath) {
		return getIFLExpensive(null, absolutePath);
	}

	/**
	 * Returns an IIndexFileLocation by searching the workspace for resources that are mapped
	 * onto the specified absolute path.
	 * <p>
	 * If such a resource exists, an IIndexFileLocation that
	 * contains both the resource location URI, and the resources full path is created.
	 * <p>
	 * Otherwise, an IIndexFileLocation which contains the absolute path in URI form is returned.
	 * <p>
	 * N.B. As this searches the workspace, following links and potentially reading from alternate
	 * file systems, this method may be expensive.
	 * @param cproject the ICProject to prefer when resolving external includes to workspace
	 *   resources (may be null)
	 * @param absolutePath
	 * @return an IIndexFileLocation for the specified resource, containing a workspace relative
	 *   path if possible.
	 */
	public static IIndexFileLocation getIFLExpensive(ICProject cproject, String absolutePath) {
		final IProject preferredProject = cproject == null ? null : cproject.getProject();
		IFile file = ResourceLookup.selectFileForLocation(new Path(absolutePath), preferredProject);
		if (file != null && file.exists())
			return getWorkspaceIFL(file);

		return getExternalIFL(absolutePath);
	}

	/**
	 * Returns an IIndexFileLocation for the specified absolute path, with no associated full path.
	 * @param absolutePath
	 * @return an IIndexFileLocation for the specified absolute path, with no associated full path.
	 */
	public static IIndexFileLocation getExternalIFL(String absolutePath) {
		return getExternalIFL(new Path(absolutePath));
	}

	/**
	 * Returns an IIndexFileLocation for the specified absolute path, with no associated full path.
	 * @param absolutePath
	 * @return an IIndexFileLocation for the specified absolute path, with no associated full path.
	 */
	public static IIndexFileLocation getExternalIFL(IPath absolutePath) {
		return new IndexFileLocation(UNCPathConverter.getInstance().toURI(absolutePath), null);
	}

	/**
	 * Returns an IIndexFileLocation for the specified workspace file, or <code>null</code>
	 * if it does not have a location.
	 * @param file
	 * @return an IIndexFileLocation for the specified workspace file
	 */
	public static IIndexFileLocation getWorkspaceIFL(IFile file) {
		final URI locationURI = file.getLocationURI();
		if (locationURI != null) {
			return new IndexFileLocation(locationURI, file.getFullPath().toString());
		}
		return null;
	}

	/**
	 * Returns<ul>
	 * <li> a workspace IIndexFileLocation if the translation unit has an associated resource
	 * <li> an external IIndexFileLocation if the translation unit does not have an associated resource
	 * <li> null, in any other case
	 * </ul>
	 * @param tu
	 * @return a suitable IIndexFileLocation for the specified ITranslationUnit
	 */
	public static IIndexFileLocation getIFL(ITranslationUnit tu) {
		IResource res = tu.getResource();
		if (res instanceof IFile) {
			return getWorkspaceIFL((IFile) res);
		}
		IPath location = tu.getLocation();
		if (location != null) {
			return getExternalIFL(location);
		}
		return null;
	}
}
