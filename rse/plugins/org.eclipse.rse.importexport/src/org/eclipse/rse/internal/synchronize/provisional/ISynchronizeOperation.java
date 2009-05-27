/*******************************************************************************
 * Copyright (c) 2008, 2009 Takuya Miyamoto and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Takuya Miyamoto - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.synchronize.provisional;

import java.util.Calendar;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;

/**
 * Utility class to synchronize local and remote resources. IResource is used as
 * local resources. IRemoteFile is used as remote resources. There are some
 * kinds of synchronization, such as Import, Export, Synchronize, or Manual
 * Synchronize.
 */
public interface ISynchronizeOperation {
	/**
	 * the mode of synchronization is not defined
	 */
	public static final int SYNC_MODE_NON = 0;
	/**
	 * "Import": Always override source with destination
	 */
	public static final int SYNC_MODE_OVERRIDE_SOURCE = 1;
	/**
	 * "Export": Always override destination with source.
	 */
	public static final int SYNC_MODE_OVERRIDE_DEST = 2;
	/**
	 * "Re-Synchronize": Always override older files with newer ones
	 */
	public static final int SYNC_MODE_OVERRIDE_OLDER = 3;
	/**
	 * "Manual Re-Synchronize":Review sync in UI
	 */
	public static final int SYNC_MODE_UI_REVIEW = 4;

	/**
	 * TODO This will be removed because initial synchronize will be import or
	 * export. "Initial Manual Synchronize":Review sync in UI.
	 */
	public static final int SYNC_MODE_UI_REVIEW_INITIAL = 5;

	/**
	 * Synchronize local with remote, using the given filter. Both local and
	 * remote are folder. If lastSyncDate != null, it is the timestamp of last
	 * synchronization (for 3-way synchronization). The filter is always applied
	 * to the all kinds of synchronize operation. In initial export operation,
	 * filter is applied to the exported local resources. In initial import
	 * operation, filter is applied to the imported remote resources. In
	 * synchronization, filter is applied to the synchronized resources. throws
	 * CoreException if any error occurs. When SYNC_MODE_UI_REVIEW is requested
	 * option, the method returns as soon as the Synchronize View has been
	 * filled with contents according to the request.
	 * 
	 * @param local
	 * @param remote
	 * @param filter
	 * @param lastSyncDate
	 * @param options
	 * @throws CoreException
	 */
	// TODO currently lastSyncDate is unnecessary
	public void synchronize(List<IResource> local, IRemoteFile remote, ISynchronizeFilter filter, Calendar lastSyncDate, int options) throws CoreException;

}
