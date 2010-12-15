/*******************************************************************************
 * Copyright (c) 2010 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	  Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.sourcelookup;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.sourcelookup.IMappingSourceContainer;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.CompositeSourceContainer;
import org.eclipse.osgi.util.NLS;

/**
 * A project where source folders are used for running the C/C++ compiler. This container
 * is intended to be used when debugging information produced by the C/C++ compiler contains paths
 * to the source and header files relative to the directory where the compiler is run from.
 * The assumption is that all files under a source folder are compiled relative to that folder.
 * 
 * Source elements returned from <code>findSourceElements(...)</code> are instances of
 * <code>LocalFileStorage</code>.
 * <p>
 * Clients may instantiate this class. 
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class SourceFoldersRelativePathSourceContainer extends CompositeSourceContainer
		implements IMappingSourceContainer {
	/**
	 * Unique identifier for the container type
	 * (value <code>org.eclipse.cdt.debug.core.containerType.sourceFoldersRelativePath</code>).
	 */
	public static final String TYPE_ID =
			CDebugCorePlugin.getUniqueIdentifier() + ".containerType.sourceFoldersRelativePath"; //$NON-NLS-1$

	private final IProject fProject;
	private boolean fSearchReferencedProjects;

	/**
	 * Constructs a source folder relative path source container.
	 * 
	 * @param project the project to search for source in. A {@code null} project indicates
	 * 		the the project from the launch configuration should be used.
	 * @param referenced whether referenced projects should be considered
	 */
	public SourceFoldersRelativePathSourceContainer(IProject project, boolean referenced) {
		fProject = project;
		fSearchReferencedProjects = referenced;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#isComposite()
	 */
	public boolean isComposite() {
		return true;
	}

	/**
	 * Returns whether referenced projects are considered.
	 * 
	 * @return whether referenced projects are considered
	 */
	public boolean isSearchReferencedProjects() {
		return fSearchReferencedProjects;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getName()
	 */
	public String getName() {
		IProject project = getResolvedProject();
		return project == null ?
				InternalSourceLookupMessages.SourceFoldersRelativePathSourceContainer_0 :
				NLS.bind(InternalSourceLookupMessages.SourceFoldersRelativePathSourceContainer_1, project.getName());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#getType()
	 */
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof SourceFoldersRelativePathSourceContainer) {
			SourceFoldersRelativePathSourceContainer loc = (SourceFoldersRelativePathSourceContainer) obj;
			return fProject == null ? loc.fProject == null : fProject.equals(loc.fProject);
		}	
		return false;
	}	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return TYPE_ID.hashCode() * 31 + (fProject == null ? 0 : fProject.hashCode());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.IMappingSourceContainer#getCompilationPath(java.lang.String)
	 */
	public IPath getCompilationPath(String sourceName) {
		IProject project = getResolvedProject();
		if (project == null)
			return null;
		ICProject cProject = CModelManager.getDefault().create(project);
		IPath path = new Path(sourceName);
		for (IFile file : ResourceLookup.findFilesForLocation(path)) {
			ISourceRoot root = cProject.findSourceRoot(file);
			if (root != null) {
				return path.makeRelativeTo(root.getResource().getLocation());
			}
		}
		return null;
	}

	public IProject getProject() {
		return fProject;
	}

	/**
	 * Returns the project associated with this source container either directly or through
	 * the current launch configuration.
	 * @return an IProject instance or {@code null}.
	 */
	private IProject getResolvedProject() {
		if (fProject != null)
			return fProject;
		ISourceLookupDirector director = getDirector();
		if (director != null) {
			return SourceUtils.getLaunchConfigurationProject(director);
		}
		return null;
	}

	@Override
	protected ISourceContainer[] createSourceContainers() throws CoreException {
		IProject project = getResolvedProject();
		if (project != null && project.isOpen()) {
			if (isSearchReferencedProjects()) {
				IProject[] projects = SourceUtils.getAllReferencedProjects(project);
				ISourceContainer[] folders = createCompilationDirectoryContainers(project);
				List<ISourceContainer> containers = new ArrayList<ISourceContainer>(folders.length + projects.length);
				for (ISourceContainer folder : folders) {
					containers.add(folder);
				}
				for (IProject ref : projects) {
					if (ref.exists() && ref.isOpen()) {
						SourceFoldersRelativePathSourceContainer container =
								new SourceFoldersRelativePathSourceContainer(ref, false);
						container.init(getDirector());
						containers.add(container);
					}
				}
				return containers.toArray(new ISourceContainer[containers.size()]);
			} 
			return createCompilationDirectoryContainers(project);
		}
		return new ISourceContainer[0];
	}

	private ISourceContainer[] createCompilationDirectoryContainers(IProject project) throws CoreException {
		ICProject cProject = CModelManager.getDefault().create(project);
		ISourceRoot[] roots = cProject.getAllSourceRoots();
		List<ISourceContainer> list = new ArrayList<ISourceContainer>(roots.length);
		for (ISourceRoot root : roots) {
			IContainer folder = (IContainer) root.getResource();
			ISourceContainer container = new CompilationDirectorySourceContainer(folder.getLocation(), false);
			container.init(getDirector());
			list.add(container);
		}
		return list.toArray(new ISourceContainer[list.size()]);
	}
}
