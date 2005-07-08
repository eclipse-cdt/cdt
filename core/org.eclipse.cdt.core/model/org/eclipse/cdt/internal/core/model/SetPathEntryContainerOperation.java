/*******************************************************************************
 * Copyright (c) 2005 QnX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Qnx Software Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IContainerEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IPathEntryContainer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class SetPathEntryContainerOperation extends CModelOperation {

	IPathEntryContainer newContainer;
	ICProject[] affectedProjects;
	PathEntryManager fPathEntryManager;

	public SetPathEntryContainerOperation(ICProject[] affectedProjects, IPathEntryContainer newContainer) {
		super(affectedProjects);
		this.affectedProjects = affectedProjects;
		this.newContainer = newContainer;
		fPathEntryManager = PathEntryManager.getDefault();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CModelOperation#isReadOnly()
	 */
	public boolean isReadOnly() {
		return true;
	}

	protected void executeOperation() throws CModelException {
		if (isCanceled()) {
			return;
		}
		
		IPath containerPath = (newContainer == null) ? new Path("") : newContainer.getPath(); //$NON-NLS-1$
		final int projectLength = affectedProjects.length;
		final ICProject[] modifiedProjects = new ICProject[projectLength];
		System.arraycopy(affectedProjects, 0, modifiedProjects, 0, projectLength);
		final IPathEntry[][] oldResolvedEntries = new IPathEntry[projectLength][];
		// filter out unmodified project containers
		int remaining = 0;
		for (int i = 0; i < projectLength; i++) {
			if (isCanceled()) {
				return;
			}
			ICProject affectedProject = affectedProjects[i];
			boolean found = false;
			IPathEntry[] rawPath = fPathEntryManager.getRawPathEntries(affectedProject);
			for (int j = 0, cpLength = rawPath.length; j < cpLength; j++) {
				IPathEntry entry = rawPath[j];
				if (entry.getEntryKind() == IPathEntry.CDT_CONTAINER) {
					IContainerEntry cont = (IContainerEntry)entry;
					if (cont.getPath().equals(containerPath)) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				// filter out this project - does not reference the container
				// path
				modifiedProjects[i] = null;
				// Still add it to the cache
				fPathEntryManager.containerPut(affectedProject, containerPath, newContainer);
				continue;
			}
			IPathEntryContainer oldContainer = fPathEntryManager.containerGet(affectedProject, containerPath, true);
			if (oldContainer != null && newContainer != null && oldContainer.equals(newContainer)) {
				modifiedProjects[i] = null; // filter out this project -
				// container did not change
				continue;
			}
			remaining++;
			oldResolvedEntries[i] = fPathEntryManager.removeCachedResolvedPathEntries(affectedProject);
			fPathEntryManager.containerPut(affectedProject, containerPath, newContainer);
		}

		// Nothing change.
		if (remaining == 0) {
			return;
		}

		// trigger model refresh

		CModelManager mgr = CModelManager.getDefault();
		for (int i = 0; i < projectLength; i++) {
			if (isCanceled()) {
				return;
			}
			ICProject affectedProject = modifiedProjects[i];
			if (affectedProject == null) {
				continue; // was filtered out
			}
			// Only fire deltas if we had previous cache
			if (oldResolvedEntries[i] != null) {
				IPathEntry[] newEntries = fPathEntryManager.getResolvedPathEntries(affectedProject);
				ICElementDelta[] deltas = fPathEntryManager.generatePathEntryDeltas(affectedProject, oldResolvedEntries[i], newEntries);
				if (deltas.length > 0) {
					affectedProject.close();
					//shouldFire = true;
					for (int j = 0; j < deltas.length; j++) {
						addDelta(deltas[j]);
					}
				}
			}
		}

	}

}
