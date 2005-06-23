/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.browser;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class TypeSearchScope implements ITypeSearchScope {

	private Set fPathSet = new HashSet();
	private Set fContainerSet = new HashSet();
	private Set fProjectSet = new HashSet();
	private Set fEnclosingProjectSet = new HashSet();
	private boolean fWorkspaceScope = false;

	// cached arrays
	private IProject[] fAllProjects = null;
	private IProject[] fProjects = null;
	private IPath[] fContainerPaths = null;

	public TypeSearchScope() {
	}

	public TypeSearchScope(boolean workspaceScope) {
		fWorkspaceScope = workspaceScope;
	}

	public TypeSearchScope(ITypeSearchScope scope) {
		add(scope);
	}
	
	public TypeSearchScope(IProject project) {
		add(project);
	}

	public Collection pathSet() {
		return fPathSet;
	}
	public Collection containerSet() {
		return fContainerSet;
	}
	public Collection projectSet() {
		return fProjectSet;
	}
	public Collection enclosingProjectSet() {
		return fEnclosingProjectSet;
	}

	public boolean encloses(ITypeSearchScope scope) {
		if (isWorkspaceScope())
			return true;
		
		if (!scope.pathSet().isEmpty()) {
			// check if this scope encloses the other scope's paths
			for (Iterator i = scope.pathSet().iterator(); i.hasNext(); ) {
				IPath path = (IPath) i.next();
				if (!encloses(path))
					return false;
			}
		}
		
		if (!scope.containerSet().isEmpty()) {
			// check if this scope encloses the other scope's containers
			for (Iterator i = scope.containerSet().iterator(); i.hasNext(); ) {
				IPath path = (IPath) i.next();
				if (!encloses(path))
					return false;
			}
		}

		if (!scope.projectSet().isEmpty()) {
			// check if this scope encloses the other scope's projects
			for (Iterator i = scope.projectSet().iterator(); i.hasNext(); ) {
				IProject project = (IProject) i.next();
				if (!encloses(project))
					return false;
			}
		}

		return true;
	}

	public boolean encloses(IProject project) {
		if (isWorkspaceScope())
			return true;
		
		// check projects that were explicity added to scope
		if (fProjectSet.contains(project))
			return true;
		
		return false;
	}
	
	public boolean encloses(IPath path) {
		if (isWorkspaceScope())
			return true;

		// check files that were explicity added to scope
		if (fPathSet.contains(path))
			return true;

		// check containers that were explicity added to scope
		// including subdirs
		if (fContainerSet.contains(path))
			return true;
		if (fContainerPaths == null) {
			fContainerPaths = (IPath[]) fContainerSet.toArray(new IPath[fContainerSet.size()]);
//			java.util.Arrays.sort(fContainerPaths);
		}
		for (int i = 0; i < fContainerPaths.length; ++i) {
			if (fContainerPaths[i].isPrefixOf(path)) {
				return true;
			}
		}

		// check projects that were explicity added to scope
		if (fProjectSet.contains(path))
			return true;

		// check projects that were explicity added to scope
		if (fProjects == null) {
			fProjects = (IProject[]) fProjectSet.toArray(new IProject[fProjectSet.size()]);
		}
		// check if one of the projects contains path
		for (int i = 0; i < fProjects.length; ++i) {
			if (projectContainsPath(fProjects[i], path, false)) {
				return true;
			}
		}
		
		return false;
	}

	public boolean encloses(String path) {
		return encloses(new Path(path));
	}

	public boolean encloses(ICElement element) {
		return encloses(element.getPath());
	}

	public boolean encloses(IWorkingCopy workingCopy) {
		return encloses(workingCopy.getOriginalElement().getPath());
	}
	
	public IProject[] getEnclosingProjects() {
		if (isWorkspaceScope()) {
			return getAllProjects();
		}
		return (IProject[]) fEnclosingProjectSet.toArray(new IProject[fEnclosingProjectSet.size()]);
	}
	
	private static boolean projectContainsPath(IProject project, IPath path, boolean checkIncludePaths) {
		IPath projectPath = project.getFullPath();
		if (projectPath.isPrefixOf(path)) {
//			ISourceRoot[] sourceRoots = null;
//			try {
//				sourceRoots = cProject.getSourceRoots();
//			} catch (CModelException ex) {
//			}
//			if (sourceRoots != null) {
//				for (int j = 0; j < sourceRoots.length; ++j) {
//					ISourceRoot root = sourceRoots[j];
//					if (root.isOnSourceEntry(path))
//						return true;
//				}
//			}
			return true;
		}

		if (checkIncludePaths) {
			//TODO this appears to be very slow -- cache this?
			IPath[] includePaths = getIncludePaths(project);
			if (includePaths != null) {
				for (int i = 0; i < includePaths.length; ++i) {
					IPath include = includePaths[i];
					if (include.isPrefixOf(path) || include.equals(path))
						return true;
				}
			}
		}
		
		return false;
	}

	private static IPath[] getIncludePaths(IProject project) {
		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
		if (provider != null) {
			IScannerInfo info = provider.getScannerInformation(project);
			if (info != null) {
				String[] includes = info.getIncludePaths();
				if (includes != null && includes.length > 0) {
					IPath[] includePaths = new IPath[includes.length];
					for (int i = 0; i < includes.length; ++i) {
						includePaths[i] = new Path(includes[i]);
					}
//					java.util.Arrays.sort(includePaths);
					return includePaths;
				}
			}
		}
		return null;
	}

	private static IProject[] getAllProjects() {
		IProject[] projects = getCProjects();
		if (projects == null)
			projects = new IProject[0];
		return projects;
	}
	
	private static IProject[] getCProjects() {
		IProject[] allProjects = CCorePlugin.getWorkspace().getRoot().getProjects();
		if (allProjects != null) {
			IProject[] cProjects = new IProject[allProjects.length];
			int count = 0;
			for (int i = 0; i < allProjects.length; ++i) {
				IProject project = allProjects[i];
				if (isCProject(project)) {
					cProjects[count++] = project;
				}
			}
			if (count > 0) {
				if (count == allProjects.length) {
					return cProjects;
				}
				IProject[] newProjects = new IProject[count];
				System.arraycopy(cProjects, 0, newProjects, 0, count);
				return newProjects;
			}
		}
		return null;
	}
	
	private static boolean isCProject(IProject project) {
		IProjectDescription projDesc = null;
		try {
			projDesc = project.getDescription();
			if (projDesc == null)
				return false;
		} catch (CoreException e) {
			return false;
		}
		String[] natures = projDesc.getNatureIds();
		if (natures != null) {
			for (int i = 0; i < natures.length; ++i) {
				if (natures[i].equals(CProjectNature.C_NATURE_ID)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isPathScope() {
		return !fPathSet.isEmpty();
	}

	public boolean isProjectScope() {
		return !fProjectSet.isEmpty();
	}

	public boolean isWorkspaceScope() {
		return fWorkspaceScope;
	}

	public boolean isEmpty() {
		return (!isWorkspaceScope() && fPathSet.isEmpty() && fContainerSet.isEmpty() && fProjectSet.isEmpty());
	}
	
	public void add(IWorkingCopy workingCopy) {
		IPath path = workingCopy.getOriginalElement().getPath();
		IProject enclosingProject = null;
		ICProject cProject = workingCopy.getCProject();
		if (cProject != null)
			enclosingProject = cProject.getProject();
		fPathSet.add(path);
		if (enclosingProject != null)
			addEnclosingProject(enclosingProject);
	}

	public void add(IPath path, boolean addSubfolders, IProject enclosingProject) {
		if (addSubfolders) {
			fContainerSet.add(path);
			fContainerPaths = null;
		} else {
			fPathSet.add(path);
		}
		if (enclosingProject != null) {
			addEnclosingProject(enclosingProject);
		} else {
			// check all projects in workspace
			if (fAllProjects == null) {
				fAllProjects = getAllProjects();
			}
			// check if one of the projects contains path
			for (int i = 0; i < fAllProjects.length; ++i) {
				if (projectContainsPath(fAllProjects[i], path, false)) {
					addEnclosingProject(fAllProjects[i]);
					break;
				}
			}
		}
	}

	public void add(IProject project) {
		fProjectSet.add(project);
		fProjects = null;
		fAllProjects = null;
		addEnclosingProject(project);
	}

	private void addEnclosingProject(IProject project) {
		fEnclosingProjectSet.add(project);
	}

	public void addWorkspace() {
		fWorkspaceScope = true;
		fProjects = null;
		fAllProjects = null;
	}
	
	public void add(ICElement elem) {
		if (elem == null)
			return;

		switch (elem.getElementType()) {
			case ICElement.C_MODEL: {
				addWorkspace();
				break;
			}
			
			case ICElement.C_PROJECT: {
				IProject project = ((ICProject)elem).getProject();
				add(project);
				break;
			}
			
			case ICElement.C_CCONTAINER: {
				IProject project = null;
				ICProject cProject = elem.getCProject();
				if (cProject != null)
					project = cProject.getProject();
				add(elem.getPath(), true, project);
				break;
			}

			case ICElement.C_UNIT: {
				IProject project = null;
				ICProject cProject = elem.getCProject();
				if (cProject != null)
					project = cProject.getProject();
				add(elem.getPath(), false, project);
				break;
			}
			
			case ICElement.C_INCLUDE:
			case ICElement.C_NAMESPACE:
			case ICElement.C_TEMPLATE_CLASS:
			case ICElement.C_CLASS:
			case ICElement.C_STRUCT:
			case ICElement.C_UNION:
			case ICElement.C_ENUMERATION:
			case ICElement.C_TYPEDEF: {
				IProject project = null;
				ICProject cProject = elem.getCProject();
				if (cProject != null)
					project = cProject.getProject();
				add(elem.getPath(), false, project);
				break;
			}
		}
	}

	public void add(ITypeSearchScope scope) {
		fPathSet.addAll(scope.pathSet());
		fContainerSet.addAll(scope.containerSet());
		fProjectSet.addAll(scope.projectSet());
		fEnclosingProjectSet.addAll(scope.enclosingProjectSet());
		fProjects = null;
		fAllProjects = null;
		fContainerPaths = null;
		fWorkspaceScope |= scope.isWorkspaceScope();
	}
	
	public void clear() {
		fPathSet.clear();
		fContainerSet.clear();
		fProjectSet.clear();
		fEnclosingProjectSet.clear();
		fWorkspaceScope = false;
		fProjects = null;
		fAllProjects = null;
		fContainerPaths = null;
	}
}
