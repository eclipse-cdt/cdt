/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.browser;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

public interface ITypeReference {

	/**
	 * Returns the full, absolute path of this reference
	 * relative to the workspace, or null if no path can be
	 * determined. 
	 */
	public IPath getPath();

	 /**
	 * Returns the absolute path in the local file system
	 * to this reference, or null if no path can be
	 * determined. 
	 */
	public IPath getLocation();

	/**
	 * Returns the resource.
	 */
	public IResource getResource();

	/**
	 * Returns the working copy.
	 */
	public IWorkingCopy getWorkingCopy();

	/**
	 * Returns the project.
	 */
	public IProject getProject();

	/**
	 * Returns the offset.
	 */
	public int getOffset();

	/**
	 * Returns the length.
	 */
	public int getLength();
	
	/**
	 * Returns the CElements located at the stored offset and length,
	 * or <code>null</code> if not found.
	 */
	public ICElement[] getCElements();
	
	/**
	 * Returns a translation unit for this location.
	 */
	public ITranslationUnit getTranslationUnit();

	/** Gets the path for this location, relative to one of
	 * the given project's include paths.
	 * 
	 * @param project the project to use as a reference.
	 * @return The path to this location, relative to the longest
	 * matching include path in the given project.
	 */
	public IPath getRelativeIncludePath(IProject project);

	/** Gets the path for this location, relative to the
	 * given path.
	 * 
	 * @param relativeToPath the path to use as a reference.
	 * @return The path to this location, relative to the
	 * given path.
	 */
	public IPath getRelativePath(IPath relativeToPath);
}
