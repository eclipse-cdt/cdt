/*******************************************************************************
 * Copyright (c) 2006, 2013 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.index;

import java.util.Set;

import org.eclipse.cdt.core.model.ICProject;

/**
 * IndexChangeEvents describe changes to the index.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 *
 * @since 4.0
 */
public interface IIndexChangeEvent {
	/**
	 * Returns the project for which the index has changed.
	 */
	public ICProject getAffectedProject();

	/**
	 * Returns <code>true</code> when the index for the project was loaded for the first time or
	 * reloaded with a different database.
	 */
	public boolean isReloaded();

	/**
	 * Returns <code>true</code> when the index for the project was cleared.
	 */
	public boolean isCleared();

	/**
	 * Returns the set of files that has been cleared in the index. When {@link #isCleared()}
	 * returns <code>true</code>, the set will be empty.
	 */
	public Set<IIndexFileLocation> getFilesCleared();

	/**
	 * Returns the set of files that has been added or updated. When {@link #isCleared()} returns
	 * <code>true</code>, the files of the set have been written after the index was cleared.
	 */
	public Set<IIndexFileLocation> getFilesWritten();

	/**
	 * Returns <code>true</code> when a new file had been added to the index.
	 * @since 5.2
	 */
	public boolean hasNewFile();
}
