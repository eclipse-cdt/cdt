/*******************************************************************************
 * Copyright (c) 2004, 2009 QNX Software Systems and others.
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
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class TypeSearchScope implements ITypeSearchScope {

	private Set<IPath> fPathSet = new HashSet<IPath>();
	private Set<IPath> fContainerSet = new HashSet<IPath>();
	private Set<ICProject> fProjectSet = new HashSet<ICProject>();
	private Set<ICProject> fEnclosingProjectSet = new HashSet<ICProject>();
	private boolean fWorkspaceScope = false;

	// cached arrays
	private ICProject[] fAllProjects = null;
	private ICProject[] fProjects = null;
	private IPath[] fContainerPaths = null;

	public TypeSearchScope() {
	}

	public TypeSearchScope(boolean workspaceScope) {
		fWorkspaceScope = workspaceScope;
	}

	public TypeSearchScope(ITypeSearchScope scope) {
		add(scope);
	}
	
	public TypeSearchScope(ICProject project) {
		add(project);
	}

	@Override
	public Collection<IPath> pathSet() {
		return fPathSet;
	}
	@Override
	public Collection<IPath> containerSet() {
		return fContainerSet;
	}
	@Override
	public Collection<ICProject> projectSet() {
		return fProjectSet;
	}
	@Override
	public Collection<ICProject> enclosingProjectSet() {
		return fEnclosingProjectSet;
	}

	@Override
	public boolean encloses(ITypeSearchScope scope) {
		if (isWorkspaceScope())
			return true;
		
		if (!scope.pathSet().isEmpty()) {
			// check if this scope encloses the other scope's paths
			for (Iterator<IPath> i = scope.pathSet().iterator(); i.hasNext(); ) {
				IPath path = i.next();
				if (!encloses(path))
					return false;
			}
		}
		
		if (!scope.containerSet().isEmpty()) {
			// check if this scope encloses the other scope's containers
			for (Iterator<IPath> i = scope.containerSet().iterator(); i.hasNext(); ) {
				IPath path = i.next();
				if (!encloses(path))
					return false;
			}
		}

		if (!scope.projectSet().isEmpty()) {
			// check if this scope encloses the other scope's projects
			for (Iterator<ICProject> i = scope.projectSet().iterator(); i.hasNext(); ) {
				ICProject project = i.next();
				if (!encloses(project))
					return false;
			}
		}

		return true;
	}

	@Override
	public boolean encloses(ICProject project) {
		if (isWorkspaceScope())
			return true;
		
		// check projects that were explicity added to scope
		if (fProjectSet.contains(project))
			return true;
		
		return false;
	}
	
	@Override
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
			fContainerPaths = fContainerSet.toArray(new IPath[fContainerSet.size()]);
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
			fProjects = fProjectSet.toArray(new ICProject[fProjectSet.size()]);
		}
		// check if one of the projects contains path
		for (int i = 0; i < fProjects.length; ++i) {
			if (projectContainsPath(fProjects[i], path, false)) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean encloses(String path) {
		return encloses(new Path(path));
	}

	@Override
	public boolean encloses(ICElement element) {
		return encloses(element.getPath());
	}

	@Override
	public boolean encloses(IWorkingCopy workingCopy) {
		return encloses(workingCopy.getOriginalElement().getPath());
	}
	
	@Override
	public ICProject[] getEnclosingProjects() {
		if (isWorkspaceScope()) {
			return getAllProjects();
		}
		return fEnclosingProjectSet.toArray(new ICProject[fEnclosingProjectSet.size()]);
	}
	
	private static boolean projectContainsPath(ICProject project, IPath path, boolean checkIncludePaths) {
		IPath projectPath = project.getProject().getFullPath();
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

	private static IPath[] getIncludePaths(ICProject project) {
		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project.getProject());
		if (provider != null) {
			IScannerInfo info = provider.getScannerInformation(project.getProject());
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

	private static ICProject[] getAllProjects() {
		ICProject[] projects = getCProjects();
		if (projects == null)
			projects = new ICProject[0];
		return projects;
	}
	
	private static ICProject[] getCProjects() {
		try {
			return CoreModel.getDefault().getCModel().getCProjects();
		} catch (CModelException e) {
			CCorePlugin.log(e);
			return new ICProject[0];
		}
	}
	
	@Override
	public boolean isPathScope() {
		return !fPathSet.isEmpty();
	}

	@Override
	public boolean isProjectScope() {
		return !fProjectSet.isEmpty();
	}

	@Override
	public boolean isWorkspaceScope() {
		return fWorkspaceScope;
	}

	@Override
	public boolean isEmpty() {
		return (!isWorkspaceScope() && fPathSet.isEmpty() && fContainerSet.isEmpty() && fProjectSet.isEmpty());
	}
	
	@Override
	public void add(IWorkingCopy workingCopy) {
		IPath path = workingCopy.getOriginalElement().getPath();
		ICProject cProject = workingCopy.getCProject();
		fPathSet.add(path);
		addEnclosingProject(cProject);
	}

	@Override
	public void add(IPath path, boolean addSubfolders, ICProject enclosingProject) {
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

	@Override
	public void add(ICProject project) {
		fProjectSet.add(project);
		fProjects = null;
		fAllProjects = null;
		addEnclosingProject(project);
	}

	private void addEnclosingProject(ICProject project) {
		fEnclosingProjectSet.add(project);
	}

	@Override
	public void addWorkspace() {
		fWorkspaceScope = true;
		fProjects = null;
		fAllProjects = null;
	}
	
	@Override
	public void add(ICElement elem) {
		if (elem == null)
			return;

		switch (elem.getElementType()) {
			case ICElement.C_MODEL: {
				addWorkspace();
				break;
			}
			
			case ICElement.C_PROJECT: {
				ICProject project = ((ICProject)elem);
				add(project);
				break;
			}
			
			case ICElement.C_CCONTAINER: {
				ICProject project = elem.getCProject();
				add(elem.getPath(), true, project);
				break;
			}

			case ICElement.C_UNIT: {
				ICProject project = elem.getCProject();
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
				ICProject project = elem.getCProject();
				add(elem.getPath(), false, project);
				break;
			}
		}
	}

	@Override
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
	
	@Override
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
