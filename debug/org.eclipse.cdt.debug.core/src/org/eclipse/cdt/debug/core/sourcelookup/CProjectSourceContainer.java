/*******************************************************************************
 * Copyright (c) 2003, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - Mikhail Khodjaiants - Bug 80857
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.debug.core.sourcelookup;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.internal.core.sourcelookup.InternalSourceLookupMessages;
import org.eclipse.cdt.debug.internal.core.sourcelookup.SourceUtils;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.CompositeSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;

/**
 * A project in the workspace. Source is searched for in the root project
 * folder and all folders within the project recursively. Optionally,
 * referenced projects may be searched as well.
 * 
 * Source elements returned from <code>findSourceElements(...)</code> are instances of
 * <code>IFile</code>.
 * <p>
 * Clients may instantiate this class.
 * </p>
 * @since 7.1
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CProjectSourceContainer extends CompositeSourceContainer {
	/**
	 * Unique identifier for the project source container type
	 * (value <code>org.eclipse.cdt.debug.core.containerType.project</code>).
	 */
	public static final String TYPE_ID =
			CDebugCorePlugin.getUniqueIdentifier() + ".containerType.project"; //$NON-NLS-1$
	private final IProject fOwnProject;  // Project assigned to this container at construction time.
	private IProject fProject;
	private boolean fSearchReferencedProjects;
	private URI fRootURI;
	private IFileStore fRootFile;
	private IWorkspaceRoot fRoot;

	/**
	 * Constructs a project source container.
	 * 
	 * @param project the project to search for source in
	 * @param referenced whether referenced projects should be considered
	 */
	public CProjectSourceContainer(IProject project, boolean referenced) {
		fOwnProject = project;
		fProject = project;
		fSearchReferencedProjects = referenced;
	}

	/**
	 * Returns the project this source container references.
	 * 
	 * @return the project this source container references
	 */
	public IProject getProject() {
		return fProject;
	}

	@Override
	public void init(ISourceLookupDirector director) {
		super.init(director);
		if (fProject == null && director != null) {
			fProject = SourceUtils.getLaunchConfigurationProject(director);
		}
		if (fProject != null) {
			fRootURI = fProject.getLocationURI();
			if (fRootURI == null)
				return;
			try {
				fRootFile = EFS.getStore(fRootURI);
			} catch (CoreException e) {
			}
			fRoot = ResourcesPlugin.getWorkspace().getRoot();
		}
	}

	@Override
	public void dispose() {
		fProject = fOwnProject;
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#findSourceElements(java.lang.String)
	 */
	@Override
	public Object[] findSourceElements(String name) throws CoreException {
		if (fProject == null)
			return EMPTY;

		ArrayList<Object> sources = new ArrayList<Object>();

		// An IllegalArgumentException is thrown from the "getFile" method 
		// if the path created by appending the file name to the container 
		// path doesn't conform with Eclipse resource restrictions.
		// To prevent the interruption of the search procedure we check 
		// if the path is valid before passing it to "getFile".		
		if (validateFile(name)) {
			IFile file = fProject.getFile(new Path(name));
			if (file.exists()) {
				sources.add(file);
			} else {
				// See bug 82627 - perform case insensitive source lookup
				if (fRootURI == null) {
					return EMPTY;
				}
				// See bug 98090 - we need to handle relative path names
				IFileStore target = fRootFile.getFileStore(new Path(name));
				if (target.fetchInfo().exists()) {
					// We no longer have to account for bug 95832, and URIs take care
					// of canonical paths (fix to bug 95679 was removed).
					IFile[] files = fRoot.findFilesForLocationURI(target.toURI());
					if (isFindDuplicates() && files.length > 1) {
						for (IFile f : files) {
							sources.add(f);
						}
					} else if (files.length > 0) {
						sources.add(files[0]);
					}					
				}
			}
		}

		// Check sub-folders		
		if ((isFindDuplicates() && true) || (sources.isEmpty() && true)) {
			ISourceContainer[] containers = getSourceContainers();
			for (int i = 0; i < containers.length; i++) {
				Object[] objects = containers[i].findSourceElements(name);
				if (objects == null || objects.length == 0) {
					continue;
				}
				if (isFindDuplicates()) {
					for (Object object : objects)
						sources.add(object);
				} else {
					sources.add(objects[0]);
					break;
				}
			}
		}			
		
		if (sources.isEmpty())
			return EMPTY;
		return sources.toArray();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getName()
	 */
	@Override
	public String getName() {		
		return fProject != null ? fProject.getName() : InternalSourceLookupMessages.CProjectSourceContainer_0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof CProjectSourceContainer) {
			CProjectSourceContainer loc = (CProjectSourceContainer) obj;
			return fProject == null ? loc.fProject == null : fProject.equals(loc.fProject);
		}	
		return false;
	}	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return TYPE_ID.hashCode() * 31 + (fProject == null ? 0 : fProject.hashCode());
	}

	@Override
	public boolean isComposite() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.containers.CompositeSourceContainer#createSourceContainers()
	 */
	@Override
	protected ISourceContainer[] createSourceContainers() throws CoreException {
		if (fProject != null && fProject.isOpen()) {
			if (isSearchReferencedProjects()) {
				IProject[] projects = SourceUtils.getAllReferencedProjects(fProject);
				ISourceContainer[] folders = createFolderSourceContainers(fProject);
				List<ISourceContainer> containers = new ArrayList<ISourceContainer>(folders.length + projects.length);
				for (ISourceContainer folder : folders) {
					containers.add(folder);
				}
				for (IProject ref : projects) {
					if (ref.exists() && ref.isOpen()) {
						CProjectSourceContainer container = new CProjectSourceContainer(ref, false);
						container.init(getDirector());
						containers.add(container);
					}
				}
				return containers.toArray(new ISourceContainer[containers.size()]);
			} 
			return createFolderSourceContainers(fProject);
		}
		return new ISourceContainer[0];
	}

	private ISourceContainer[] createFolderSourceContainers(IProject project) throws CoreException {
		IResource[] resources = project.members();
		List<FolderSourceContainer> list = new ArrayList<FolderSourceContainer>(resources.length);
		for (IResource resource : resources) {
			if (resource.getType() == IResource.FOLDER) {
				list.add(new FolderSourceContainer((IFolder) resource, true));
			}
		}
		ISourceContainer[] containers = list.toArray(new ISourceContainer[list.size()]);
		for (ISourceContainer container : containers) {
			container.init(getDirector());
		}			
		return containers;
	}

	/**
	 * Validates the given string as a path for a file in this container. 
	 * 
	 * @param name path name
	 */
	private boolean validateFile(String name) {
		if (fProject == null) {
			return false;
		}
		IPath path = fProject.getFullPath().append(name);
		return ResourcesPlugin.getWorkspace().validatePath(path.toOSString(), IResource.FILE).isOK();
	}

	/**
	 * Returns whether referenced projects are considered.
	 * 
	 * @return whether referenced projects are considered
	 */
	public boolean isSearchReferencedProjects() {
		return fSearchReferencedProjects;
	}

	@Override
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}
}
