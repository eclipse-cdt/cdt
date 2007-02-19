/*******************************************************************************
 * Copyright (c) 2006, 2007 Symbian Software Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.index;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.index.IndexFileLocation;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Factory for obtaining instances of IIndexFileLocation for workspace or external files, and
 * some utility methods for going in the opposite direction. 
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 * 
 * @since 4.0
 */
public class IndexLocationFactory {
	/**
	 * Returns
	 * <ul>
	 * <li> the full path if this IIndexFileLocation if within the workspace root
	 * <li> the absolute path if this IIndexFileLocation is URI based and corresponds
	 * to a location on the local file system
	 * <li> otherwise, null
	 * </ul>
	 * @param location
	 * @return the workspace root relative path, a local file system absolute path or null
	 */
	public static IPath getPath(IIndexFileLocation location) {
		String fp = location.getFullPath();
		if(fp!=null) {
			return new Path(fp);
		}
		return getAbsolutePath(location);
	}
	
	/**
	 * Returns the absolute file path of an URI or null if the 
	 * URI is not a filesystem path.
	 * @param uri 
	 * @return the absolute file path of an URI or null if the 
	 * URI is not a filesystem path.
	 */
	public static IPath getAbsolutePath(IIndexFileLocation location) {
		return URIUtil.toPath(location.getURI());
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
	 * @param absolutePath
	 * @return an IIndexFileLocation for the specified resource, containing a workspace relative path if possible.
	 */
	public static IIndexFileLocation getIFLExpensive(String absolutePath) {
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(new Path(absolutePath));
		if(files.length==1) {
			return getWorkspaceIFL(files[0]);
		}
		return new IndexFileLocation(URIUtil.toURI(absolutePath), null);
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
		return new IndexFileLocation(URIUtil.toURI(absolutePath), null);	
	}
	
	/**
	 * Returns an IIndexFileLocation for the specified workspace file
	 * @param file
	 * @return an IIndexFileLocation for the specified workspace file
	 */
	public static IIndexFileLocation getWorkspaceIFL(IFile file) {
		return new IndexFileLocation(file.getLocationURI(), file.getFullPath().toString());
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
		if(res instanceof IFile) {
			return getWorkspaceIFL((IFile)res);
		} else {
			IPath location = tu.getLocation();
			if(location!=null) {
				return getExternalIFL(location);
			} else {
				return null;
			}
		}
	}
}
