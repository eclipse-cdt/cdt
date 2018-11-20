/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
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
package org.eclipse.cdt.core;

import java.net.URI;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

/**
 * An interface to manage the position tracking. It allows for mapping character
 * offsets from a file previously stored on disk to the offset in the current document
 * for the file.
 * @since 4.0
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPositionTrackerManager {
	/**
	 * Returns the position converter suitable for mapping character offsets of the
	 * given translation unit to the current version of it.
	 *
	 * @param tu a translation unit for which the position adapter is requested.
	 * @param timestamp identifies the version of the file stored on disk.
	 * @return the requested position converter or <code>null</code>.
	 */
	public IPositionConverter findPositionConverter(ITranslationUnit tu, long timestamp);

	/**
	 * Returns the position converter suitable for mapping character offsets of the
	 * given file/timestamp to the current version of it.
	 *
	 * @param file a file for which the position adapter is requested.
	 * @param timestamp identifies the version of the file stored on disk.
	 * @return the requested position converter or <code>null</code>.
	 */
	public IPositionConverter findPositionConverter(IFile file, long timestamp);

	/**
	 * Returns the position tracker suitable for mapping character offsets of the
	 * given external file/timestamp to the current version of it. <p>
	 * The method can be used for resources by supplying the <b>full path</b>. However,
	 * it does not work if you supply the location of a resource.
	 *
	 * @param fullPathOrExternalLocation an external location for which the position adapter is requested.
	 * @param timestamp identifies the version of the file stored on disk.
	 * @return the requested position converter or <code>null</code>.
	 */
	public IPositionConverter findPositionConverter(IPath fullPathOrExternalLocation, long timestamp);

	/**
	 * Returns the position tracker suitable for mapping character offsets of the
	 * given external file/timestamp to the current version of it. <p>
	 * The method cannot be used for resources that are part of the workspace.
	 *
	 * @param externalLocation an external location for which the position adapter is requested.
	 * @param timestamp identifies the version of the file stored on disk.
	 * @return the requested position converter or <code>null</code>.
	 * @since 5.1
	 */
	public IPositionConverter findPositionConverter(URI externalLocation, long timestamp);
}
