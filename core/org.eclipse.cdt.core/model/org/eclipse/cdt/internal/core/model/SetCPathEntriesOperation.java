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
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICPathEntry;
import org.eclipse.cdt.core.model.IContainerEntry;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.model.ISourceEntry;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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
		super(project);
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
		CModelManager manager = CModelManager.getDefault();
		boolean needToUpdateDependents = false;
		CElementDelta delta = new CElementDelta(getCModel());
		boolean hasDelta = false;

		// Check the removed entries.
		for (int i = 0; i < oldEntries.length; i++) {
			boolean found = false;
			for (int j = 0; j < newEntries.length; j++) {
				if (oldEntries[i].equals(newEntries[j])) {
					found = true;
					break;
				}
			}
			// Was it deleted.
			if (!found) {
				addCPathEntryDeltas(oldEntries[i], ICElementDelta.F_REMOVED_FROM_CPATHENTRY, delta);
			}
		}
		// Check the new entries.
		for (int i = 0; i < newEntries.length; i++) {
			boolean found = false;
			for (int j = 0; j < oldEntries.length; j++) {
				if (newEntries[i].equals(oldEntries[j])) {
					found = true;
					break;
				}
			}
			// is it new?
			if (!found) {
				addCPathEntryDeltas(newEntries[i], ICElementDelta.F_ADDED_TO_CPATHENTRY, delta);
			}
		}
	}

	/**
	 * Adds deltas, with the specified change flag.
	 */
	protected void addCPathEntryDeltas(ICPathEntry entry, int flag, CElementDelta delta) {
 
		int kind = entry.getEntryKind();
		ICElement celement = null;
		if (kind == ICPathEntry.CDT_SOURCE) {
			ISourceEntry source = (ISourceEntry) entry;
			IPath path = source.getSourcePath();
			celement = CoreModel.getDefault().create(path);
		} else if (kind == ICPathEntry.CDT_LIBRARY) {
			//ILibraryEntry lib = (ILibraryEntry) entry;
			//IPath path = lib.getLibraryPath();
			celement = project;
		} else if (kind == ICPathEntry.CDT_PROJECT) {
			//IProjectEntry pentry = (IProjectEntry) entry;
			//IPath path = pentry.getProjectPath();
			celement = project;
		} else if (kind == ICPathEntry.CDT_INCLUDE) {
			IIncludeEntry include = (IIncludeEntry) entry;
			IPath path = include.getResourcePath();
			celement = CoreModel.getDefault().create(path);
		} else if (kind == ICPathEntry.CDT_MACRO) {
			IMacroEntry macro = (IMacroEntry) entry;
			IPath path = macro.getResourcePath();
			celement = CoreModel.getDefault().create(path);
		} else if (kind == ICPathEntry.CDT_CONTAINER) {
			IContainerEntry container = (IContainerEntry) entry;
			celement = project;
		}
		if (celement != null) {
			delta.changed(celement, flag);
			addDelta(delta);
		}
	}
}
