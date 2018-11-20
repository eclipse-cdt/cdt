/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Chris Recoskie (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.content.IContentType;

/**
 * Contains the details of changes that occurred as a result of modifying
 * language mappings.
 *
 * @since 4.0
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ILanguageMappingChangeEvent {

	public static final int TYPE_WORKSPACE = 0;
	public static final int TYPE_PROJECT = 1;
	public static final int TYPE_FILE = 2;

	/**
	 * Returns an IFile corresponding to the file for which settings have changed if this
	 * event's type is TYPE_FILE, or null otherwise.
	 * @return an IFile corresponding to the file for which settings have changed if this
	 * event's type is TYPE_FILE, or null otherwise.
	 *
	 * @since 4.0
	 */
	public IFile getFile();

	/**
	 * Returns a String corresponding to the full path to the file for which settings have changed if this
	 * event's type is TYPE_FILE, or null otherwise.
	 * In order to obtain the full context for the file it may be required that you also call getProject(),
	 * as it is possible that this file may not live inside the workspace.
	 *
	 * @return a String corresponding to the full path to the file for which settings have changed if this
	 * event's type is TYPE_FILE, or null otherwise.
	 * @see #getProject()
	 *
	 * @since 4.0
	 */
	public String getFilename();

	/**
	 * Returns an IPath corresponding to the file for which settings have changed if this
	 * event's type is TYPE_FILE, or null otherwise.
	 * @return an IPath corresponding to the file for which settings have changed if this
	 * event's type is TYPE_FILE, or null otherwise.
	 *
	 * In order to obtain the full context for the file it may be required that you also call getProject(),
	 * as it is possible that this file may not live inside the workspace.
	 *
	 * @see #getProject()
	 *
	 * @since 4.0
	 */
	public IPath getPath();

	/**
	 * Returns an IProject corresponding to the project for which settings have changed if this
	 * event's type is TYPE_PROJECT or TYPE_FILE, or null otherwise.
	 * @return an IProject corresponding to the project for which settings have changed if this
	 * event's type is TYPE_PROJECT or TYPE_FILE, or null otherwise.
	 *
	 * @since 4.0
	 */
	public IProject getProject();

	/**
	 * Returns the type of even being reported.
	 * @return the type of even being reported
	 * @see #TYPE_WORKSPACE
	 * @see #TYPE_PROJECT
	 * @see #TYPE_FILE
	 *
	 * @since 4.0
	 */
	public int getType();

	/**
	 * Returns an array of IContentTypes for which mappings have been changed, or an empty collection
	 * if there are no affected content types.  Since there currently should be no change event unless
	 * a content type has changed, this should always contain at least one content type, but clients
	 * should theoretically be prepared to handle an empty collection.
	 * @return the content types for which mappings have been changed.
	 */
	public IContentType[] getAffectedContentTypes();
}
