/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.resources;

import org.eclipse.cdt.core.ICExtension;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.core.runtime.CoreException;

/**
 * IPathEntryStore
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPathEntryStore extends ICExtension {

	/**
	 * Returns the path entries save on the project.
	 * @throws CoreException
	 */
	IPathEntry[] getRawPathEntries() throws CoreException;

	/**
	 * Save the entries on the project.
	 * Setting paths should fire a CONTENT_CHANGED events to the listeners.
	 * It is up to the listener to calculate the deltas.
	 *
	 * @param project
	 * @param entries
	 * @throws CoreException
	 */
	void setRawPathEntries(IPathEntry[] entries) throws CoreException;

	/**
	 * Add a listener to the store.
	 *
	 * @param listener
	 */
	void addPathEntryStoreListener(IPathEntryStoreListener listener);

	/**
	 * Remove the listener form the list.
	 *
	 * @param listener
	 */
	void removePathEntryStoreListener(IPathEntryStoreListener listener);

	void close();

}
