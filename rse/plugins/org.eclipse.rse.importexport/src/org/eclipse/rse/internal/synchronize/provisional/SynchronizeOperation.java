/*******************************************************************************
 * Copyright (c) 2008 Takuya Miyamoto and others.
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
import org.eclipse.rse.internal.synchronize.filesystem.FileSystemOperations;
import org.eclipse.rse.internal.synchronize.filesystem.FileSystemProvider;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;

public class SynchronizeOperation implements ISynchronizeOperation {
	private ISynchronizePerspectiveSelector switcher;

	public SynchronizeOperation() {
		this.switcher = new SynchronizePerspectiveSelector();
	}

	public void synchronize(List<IResource> local, IRemoteFile remote, ISynchronizeFilter filter, Calendar lastSyncDate, int options) throws CoreException {

		switch (options) {
		case SYNC_MODE_OVERRIDE_DEST:
			exportTo(local, remote, filter);
			break;
		case SYNC_MODE_OVERRIDE_SOURCE:
			importFrom(local, remote, filter);
			break;
		case SYNC_MODE_OVERRIDE_OLDER:
			synchronizeWith(local, remote, filter, lastSyncDate);
			break;
		case SYNC_MODE_UI_REVIEW:
			synchronizeManually(local, remote, filter, lastSyncDate);
			break;
		case SYNC_MODE_UI_REVIEW_INITIAL:
			initialSynchronizeManually(local, remote, filter, lastSyncDate);
			break;
		}

	}

	private void importFrom(List<IResource> local, IRemoteFile remote, ISynchronizeFilter filter) throws TeamException{
			for (IResource resource : local) {
				FileSystemProvider provider = (FileSystemProvider) RepositoryProvider.getProvider(resource.getProject());
				FileSystemOperations operations = provider.getOperations();
				operations.get(new IResource[] { resource }, IResource.DEPTH_INFINITE, true, null);
			}
	}
	

	private void exportTo(List<IResource> local, IRemoteFile remote, ISynchronizeFilter filter) throws TeamException {
		for (IResource resource : local) {
			FileSystemProvider provider = (FileSystemProvider) RepositoryProvider.getProvider(resource.getProject());
			FileSystemOperations operations = provider.getOperations();
			operations.checkin(new IResource[] { resource }, IResource.DEPTH_INFINITE, false, null);
		}
	}

	private void synchronizeWith(List<IResource> local, IRemoteFile remote, ISynchronizeFilter filter, Calendar lastSyncDate) {

	}

	private void synchronizeManually(List<IResource> local, IRemoteFile remote, ISynchronizeFilter filter, Calendar lastSyncDate) {
		switcher.openSynchronizePerspective(local);
	}

	private void initialSynchronizeManually(List<IResource> local, IRemoteFile remote, ISynchronizeFilter filter, Calendar lastSyncDate) {
		switcher.openSynchronizePerspective(local);
	}

}
