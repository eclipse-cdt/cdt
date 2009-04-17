/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Takuya Miyamoto - Adapted from org.eclipse.team.examples.filesystem / PutOperation
 * David McKnight   (IBM)        - [272708] [import/export] fix various bugs with the synchronization support
 *******************************************************************************/
package org.eclipse.rse.internal.synchronize.filesystem.ui;

import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.internal.synchronize.filesystem.FileSystemProvider;
import org.eclipse.rse.internal.synchronize.filesystem.Policy;
import org.eclipse.rse.internal.synchronize.filesystem.subscriber.FileSystemSubscriber;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IDiffVisitor;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.subscribers.SubscriberScopeManager;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Operation for copying the selected resources to the file system location
 */
public class PutOperation extends FileSystemOperation {

	private boolean overwriteIncoming;

	/**
	 * Create the put operation
	 * 
	 * @param part
	 * 		the originating part
	 * @param manager
	 * 		the scope manager
	 */
	protected PutOperation(IWorkbenchPart part, SubscriberScopeManager manager) {
		super(part, manager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.rse.internal.synchronize.provisional.filesystem.ui.FileSystemOperation#execute
	 * (org.eclipse.rse.internal.synchronize.provisional.filesystem.FileSystemProvider,
	 * org.eclipse.core.resources.mapping.ResourceTraversal[],
	 * org.eclipse.core.runtime.SubProgressMonitor)
	 */
	@Override
	protected void execute(FileSystemProvider provider, ResourceTraversal[] traversals, IProgressMonitor monitor) throws CoreException {
		provider.getOperations().checkin(traversals, isOverwriteIncoming(), monitor);
		
		// refresh RSE
		IRemoteFile rootFolder = provider.getRemoteRootFolder().getRemoteFile();
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		
		try {
			rootFolder = rootFolder.getParentRemoteFileSubSystem().getRemoteFileObject(rootFolder.getAbsolutePath(), monitor);
			rootFolder.markStale(true);
			sr.fireEvent(new SystemResourceChangeEvent(rootFolder, ISystemResourceChangeEvents.EVENT_REFRESH_REMOTE, rootFolder.getAbsolutePath()));
		}
		catch (Exception e){}
		
		// if (!isOverwriteIncoming() && hasOutgoingChanges(traversals)) {
		// throw new
		// TeamException("Could not put all changes due to conflicts.");
		// }

	}

	private boolean hasOutgoingChanges(ResourceTraversal[] traversals) throws CoreException {
		final RuntimeException found = new RuntimeException();
		try {
			FileSystemSubscriber.getInstance().accept(traversals, new IDiffVisitor() {
				public boolean visit(IDiff diff) {
					if (diff instanceof IThreeWayDiff) {
						IThreeWayDiff twd = (IThreeWayDiff) diff;
						if (twd.getDirection() == IThreeWayDiff.OUTGOING || twd.getDirection() == IThreeWayDiff.CONFLICTING) {
							throw found;
						}
					}
					return false;
				}
			});
		} catch (RuntimeException e) {
			e.printStackTrace();
			if (e == found)
				return true;
			throw e;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.rse.internal.synchronize.provisional.filesystem.ui.FileSystemOperation#getTaskName
	 * ()
	 */
	@Override
	protected String getTaskName() {
		return Policy.bind("PutAction.working"); //$NON-NLS-1$
	}

	/**
	 * Return whether incoming changes should be overwritten.
	 * 
	 * @return whether incoming changes should be overwritten
	 */
	public boolean isOverwriteIncoming() {
		return overwriteIncoming;
	}

	/**
	 * Set whether incoming changes should be overwritten.
	 * 
	 * @param overwriteIncoming
	 * 		whether incoming changes should be overwritten
	 */
	public void setOverwriteIncoming(boolean overwriteIncoming) {
		this.overwriteIncoming = overwriteIncoming;
	}

}
