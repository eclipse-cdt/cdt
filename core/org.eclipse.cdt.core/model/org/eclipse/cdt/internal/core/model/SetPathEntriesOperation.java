/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;

/**
 */
public class SetPathEntriesOperation extends CModelOperation {

    /**
     * An empty array of strings indicating that a project doesn't have any prerequesite projects.
     */
    static final String[] NO_PREREQUISITES = new String[0];

	IPathEntry[] oldResolvedEntries;
	IPathEntry[] newRawEntries;
	ICProject cproject;

	public SetPathEntriesOperation(ICProject project, IPathEntry[] oldResolvedEntries, IPathEntry[] newRawEntries) {
		super(project);
		this.oldResolvedEntries = oldResolvedEntries;
		this.newRawEntries = newRawEntries;
		this.cproject = project;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CModelOperation#executeOperation()
	 */
	protected void executeOperation() throws CModelException {
		//	project reference updated - may throw an exception if unable to write .cdtproject file
		updateProjectReferencesIfNecessary();
		PathEntryManager mgr = PathEntryManager.getDefault();		
		hasModifiedResource = true;
		mgr.saveRawPathEntries(cproject, newRawEntries);
		done();
	}

	protected void updateProjectReferencesIfNecessary() throws CModelException {
		PathEntryManager mgr = PathEntryManager.getDefault();
		String[] oldRequired = mgr.projectPrerequisites(oldResolvedEntries);
		String[] newRequired = mgr.projectPrerequisites(newRawEntries);

		try {
			IProject projectResource = cproject.getProject();
			IProjectDescription description = projectResource.getDescription();

			IProject[] projectReferences = description.getReferencedProjects();

			HashSet oldReferences = new HashSet(projectReferences.length);
			for (int i = 0; i < projectReferences.length; i++) {
				String projectName = projectReferences[i].getName();
				oldReferences.add(projectName);
			}
			HashSet newReferences = (HashSet) oldReferences.clone();

			for (int i = 0; i < oldRequired.length; i++) {
				String projectName = oldRequired[i];
				newReferences.remove(projectName);
			}
			for (int i = 0; i < newRequired.length; i++) {
				String projectName = newRequired[i];
				newReferences.add(projectName);
			}

			Iterator iter;
			int newSize = newReferences.size();

			checkIdentity : {
				if (oldReferences.size() == newSize) {
					iter = newReferences.iterator();
					while (iter.hasNext()) {
						if (!oldReferences.contains(iter.next())) {
							break checkIdentity;
						}
					}
					return;
				}
			}
			String[] requiredProjectNames = new String[newSize];
			int index = 0;
			iter = newReferences.iterator();
			while (iter.hasNext()) {
				requiredProjectNames[index++] = (String) iter.next();
			}
			Arrays.sort(requiredProjectNames); // ensure that if changed, the order is consistent

			IProject[] requiredProjectArray = new IProject[newSize];
			IWorkspaceRoot wksRoot = projectResource.getWorkspace().getRoot();
			for (int i = 0; i < newSize; i++) {
				requiredProjectArray[i] = wksRoot.getProject(requiredProjectNames[i]);
			}

			description.setReferencedProjects(requiredProjectArray);
			projectResource.setDescription(description, this.fMonitor);

		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}

}
