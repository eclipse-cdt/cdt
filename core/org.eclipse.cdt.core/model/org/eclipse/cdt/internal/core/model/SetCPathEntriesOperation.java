/**********************************************************************
 * Created on Mar 25, 2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICPathEntry;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 */
public class SetCPathEntriesOperation extends CModelOperation {

	ICPathEntry[] oldEntries;
	ICPathEntry[] newEntries;
	CProject project;

	public SetCPathEntriesOperation(CProject project, ICPathEntry[] oldEntries, ICPathEntry[] newEntries) {
		this.oldEntries = oldEntries;
		this.newEntries = newEntries;
		this.project = project;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CModelOperation#executeOperation()
	 */
	protected void executeOperation() throws CModelException {
		//	project reference updated - may throw an exception if unable to write .cdtproject file
		updateProjectReferencesIfNecessary();
		try {

			ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(project.getProject());
			Element rootElement = descriptor.getProjectData(CProject.PATH_ENTRY_ID);
			// Clear out all current children
			Node child = rootElement.getFirstChild();
			while (child != null) {
				rootElement.removeChild(child);
				child = rootElement.getFirstChild();
			}

			// Save the entries
			if (newEntries != null && newEntries.length > 0) {
				// Serialize the include paths
				Document doc = rootElement.getOwnerDocument();
				project.encodeCPathEntries(doc, rootElement, newEntries);
				descriptor.saveProjectData();
			}

			generateCPathEntryDeltas();
			done();
		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}

	protected void updateProjectReferencesIfNecessary() throws CModelException {
		String[] oldRequired = this.project.projectPrerequisites(oldEntries);
		String[] newRequired = this.project.projectPrerequisites(newEntries);

		try {
			IProject projectResource = project.getProject();
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

	private void generateCPathEntryDeltas() {
	}


}
