/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Takuya Miyamoto - Adapted from org.eclipse.team.examples.filesystem / FileSystemSyncInfo
 * David McKnight   (IBM)        - [272708] [import/export] fix various bugs with the synchronization support
 *******************************************************************************/
package org.eclipse.rse.internal.synchronize.filesystem.subscriber;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;

/**
 * Provide a custom sync info that will report files that exist both locally and
 * remotely as in-sync and will return a null base if there is an incoming
 * change.
 */
public class FileSystemSyncInfo extends SyncInfo {

	public FileSystemSyncInfo(IResource local, IResourceVariant base, IResourceVariant remote, IResourceVariantComparator comparator) {
		super(local, base, remote, comparator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.team.core.subscribers.SyncInfo#calculateKind(org.eclipse.
	 * core.runtime.IProgressMonitor)
	 */
	@Override
	protected int calculateKind() throws TeamException {
		if (getLocal().getType() != IResource.FILE) {
			if (getLocal().exists() && getRemote() != null) {
				return IN_SYNC;
			}
		}
		int kind = super.calculateKind();
		if  ((kind & SyncInfo.PSEUDO_CONFLICT) != 0){
			kind = IN_SYNC;
		}

		
		return kind;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.synchronize.SyncInfo#getBase()
	 */
	@Override
	public IResourceVariant getBase() {
		// If the kind has been set and there is an incoming change
		// return null as the base since the server does not keep the
		// base contents
		// if ((getKind() & INCOMING) > 0) {
		// return null;
		// }
		return super.getBase();
	}
}
