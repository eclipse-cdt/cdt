/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.index;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.pdom.PDOMManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * Class that creates indexes based on pdoms
 * @since 4.0
 */
public class IndexFactory {
	private static final int ADD_DEPENDENCIES = IIndexManager.ADD_DEPENDENCIES;
	private static final int ADD_DEPENDENT = IIndexManager.ADD_DEPENDENT;

	private PDOMManager fPDOMManager;
	
	public IndexFactory(PDOMManager manager) {
		fPDOMManager= manager;
	}
	
	public IIndex getIndex(ICProject[] projects, int options) throws CoreException {
		boolean addDependencies= (options & ADD_DEPENDENCIES) != 0;
		boolean addDependent=    (options & ADD_DEPENDENT) != 0;
		
		HashMap map= new HashMap();
		Collection selectedProjects= getProjects(projects, addDependencies, addDependent, map, new Integer(1));
		
		ArrayList pdoms= new ArrayList();
		for (Iterator iter = selectedProjects.iterator(); iter.hasNext(); ) {
			ICProject project = (ICProject) iter.next();
			IWritableIndexFragment pdom= (IWritableIndexFragment) fPDOMManager.getPDOM(project);
			if (pdom != null) {
				pdoms.add(pdom);
			}
		}
		if (pdoms.isEmpty()) {
			return EmptyCIndex.INSTANCE;
		}
		
		// todo add the SDKs
		int primaryFragmentCount= pdoms.size();
		
		if (!addDependencies) {
			projects= (ICProject[]) selectedProjects.toArray(new ICProject[selectedProjects.size()]);
			selectedProjects.clear();
			// don't clear the map, so projects are not selected again
			selectedProjects= getProjects(projects, true, false, map, new Integer(2));
			for (Iterator iter = selectedProjects.iterator(); iter.hasNext(); ) {
				ICProject project = (ICProject) iter.next();
				IWritableIndexFragment pdom= (IWritableIndexFragment) fPDOMManager.getPDOM(project);
				if (pdom != null) {
					pdoms.add(pdom);
				}
			}
			// todo add further SDKs
		}
		
		return new CIndex((IIndexFragment[]) pdoms.toArray(new IIndexFragment[pdoms.size()]), primaryFragmentCount); 
	}

	private Collection getProjects(ICProject[] projects, boolean addDependencies, boolean addDependent, HashMap map, Integer markWith) {
		List projectsToSearch= new ArrayList();
		
		for (int i = 0; i < projects.length; i++) {
			ICProject cproject = projects[i];
			IProject project= cproject.getProject();
			checkAddProject(project, map, projectsToSearch, markWith);
			projectsToSearch.add(project);
		}
		
		if (addDependencies || addDependent) {
			for (int i=0; i<projectsToSearch.size(); i++) {
				IProject project= (IProject) projectsToSearch.get(i);
				IProject[] nextLevel;
				try {
					if (addDependencies) {
						nextLevel = project.getReferencedProjects();
						for (int j = 0; j < nextLevel.length; j++) {
							checkAddProject(nextLevel[j], map, projectsToSearch, markWith);
						}
					}
					if (addDependent) {
						nextLevel= project.getReferencingProjects();
						for (int j = 0; j < nextLevel.length; j++) {
							checkAddProject(nextLevel[j], map, projectsToSearch, markWith);
						}
					}
				} catch (CoreException e) {
					// silently ignore
					map.put(project, new Integer(0));
				}
			}
		}
		
		CoreModel cm= CoreModel.getDefault();
		Collection result= new ArrayList();
		for (Iterator iter= map.entrySet().iterator(); iter.hasNext(); ) {
			Map.Entry entry= (Map.Entry) iter.next();
			if (entry.getValue() == markWith) {
				ICProject cproject= cm.create((IProject) entry.getKey());
				if (cproject != null) {
					result.add(cproject);
				}
			}
		}
		return result;
	}

	private void checkAddProject(IProject project, HashMap map, List projectsToSearch, Integer markWith) {
		if (map.get(project) == null) {
			if (project.isOpen()) {
				map.put(project, markWith);
				projectsToSearch.add(project);
			}
			else {
				map.put(project, new Integer(0));
			}
		}
	}

	public IWritableIndex getWritableIndex(ICProject project) throws CoreException {
// mstodo to support dependent projects: Collection selectedProjects= getSelectedProjects(new ICProject[]{project}, false);
		
		Collection selectedProjects= Collections.singleton(project);
		
		ArrayList pdoms= new ArrayList();
		for (Iterator iter = selectedProjects.iterator(); iter.hasNext(); ) {
			ICProject p = (ICProject) iter.next();
			IWritableIndexFragment pdom= (IWritableIndexFragment) fPDOMManager.getPDOM(p);
			if (pdom != null) {
				pdoms.add(pdom);
			}
		}
		
		selectedProjects= getProjects(new ICProject[] {project}, true, false, new HashMap(), new Integer(1));		
		selectedProjects.remove(project);
		ArrayList readOnly= new ArrayList();
		for (Iterator iter = selectedProjects.iterator(); iter.hasNext(); ) {
			ICProject cproject = (ICProject) iter.next();
			IWritableIndexFragment pdom= (IWritableIndexFragment) fPDOMManager.getPDOM(cproject);
			if (pdom != null) {
				readOnly.add(pdom);
			}
		}
		
		
		if (pdoms.isEmpty()) {
			throw new CoreException(CCorePlugin.createStatus(
					MessageFormat.format(Messages.IndexFactory_errorNoSuchPDOM0, new Object[]{project.getElementName()})));
		}
		
		return new WritableCIndex((IWritableIndexFragment[]) pdoms.toArray(new IWritableIndexFragment[pdoms.size()]),
				(IIndexFragment[]) readOnly.toArray(new IIndexFragment[readOnly.size()]) );
	}
}
