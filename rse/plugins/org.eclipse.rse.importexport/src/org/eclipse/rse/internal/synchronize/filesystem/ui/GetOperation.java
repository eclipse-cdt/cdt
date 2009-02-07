/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Takuya Miyamoto - Adapted from org.eclipse.team.examples.filesystem / GetOperation
 *******************************************************************************/
package org.eclipse.rse.internal.synchronize.filesystem.ui;

import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.internal.synchronize.filesystem.FileSystemProvider;
import org.eclipse.rse.internal.synchronize.filesystem.Policy;
import org.eclipse.rse.internal.synchronize.filesystem.subscriber.FileSystemSubscriber;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IDiffVisitor;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.subscribers.SubscriberScopeManager;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Operation for getting the contents of the selected resources
 */
public class GetOperation extends FileSystemOperation {

	private boolean overwriteOutgoing;

	public GetOperation(IWorkbenchPart part, SubscriberScopeManager manager) {
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
		try {
			provider.getOperations().get(traversals, isOverwriteOutgoing(), monitor);
			if (!isOverwriteOutgoing() && hasIncomingChanges(traversals)) {
				throw new TeamException("Could not get all changes due to conflicts.");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private boolean hasIncomingChanges(ResourceTraversal[] traversals) throws CoreException {
		final RuntimeException found = new RuntimeException();
		try {
			FileSystemSubscriber.getInstance().accept(traversals, new IDiffVisitor() {
				public boolean visit(IDiff diff) {
					if (diff instanceof IThreeWayDiff) {
						IThreeWayDiff twd = (IThreeWayDiff) diff;
						if (twd.getDirection() == IThreeWayDiff.INCOMING || twd.getDirection() == IThreeWayDiff.CONFLICTING) {
							throw found;
						}
					}
					return false;
				}
			});
		} catch (RuntimeException e) {
			if (e == found)
				return true;
			throw e;
		}
		return false;
	}

	/**
	 * Indicate whether the operation should overwrite outgoing changes. By
	 * default, the get operation does not override local modifications.
	 * 
	 * @return whether the operation should overwrite outgoing changes.
	 */
	protected boolean isOverwriteOutgoing() {
		return overwriteOutgoing;
	}

	/**
	 * Set whether the operation should overwrite outgoing changes.
	 * 
	 * @param overwriteOutgoing
	 * 		whether the operation should overwrite outgoing changes
	 */
	public void setOverwriteOutgoing(boolean overwriteOutgoing) {
		this.overwriteOutgoing = overwriteOutgoing;
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
		return Policy.bind("GetAction.working"); //$NON-NLS-1$
	}

}
