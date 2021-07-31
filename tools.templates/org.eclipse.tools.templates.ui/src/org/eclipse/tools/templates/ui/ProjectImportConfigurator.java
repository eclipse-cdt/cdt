/*******************************************************************************
 * Copyright (c) 2021 Mat Booth and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tools.templates.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.tools.templates.core.IGenerator;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.wizards.datatransfer.ProjectConfigurator;
import org.osgi.framework.FrameworkUtil;

/**
 * Smart-import strategy for importing pre-existing projects that are not yet
 * Eclipse projects.
 *
 * Project types that have a {@link IGenerator} may extend this to offer smart
 * import functionality by registering their implementation using the <code>
 * org.eclipse.ui.ide.projectConfigurator</code> extension point.
 *
 * It is important that implementations of this class should be stateless. See
 * {@link ProjectConfigurator} for more details.
 *
 * @since 1.3
 */
public abstract class ProjectImportConfigurator implements ProjectConfigurator {

	/**
	 * Filenames that indicate a directory contains a project of your type,
	 * e.g. "CMakeLists.txt" indicates the presence of a CMake project.
	 *
	 * @return a list of filenames
	 */
	protected abstract List<String> getProjectFileNames();

	@Override
	public Set<File> findConfigurableLocations(File root, IProgressMonitor monitor) {
		final Set<File> projectFiles = new LinkedHashSet<>();
		final Set<String> dirsVisited = new HashSet<>();
		collectProjectFiles(projectFiles, root, dirsVisited, monitor);

		// LinkedHashSet preserves insertion order so it looks nice in the UI
		final Set<File> locations = new LinkedHashSet<>();
		for (File projectFile : projectFiles) {
			locations.add(projectFile.getParentFile());
		}
		return locations;
	}

	/**
	 * Recursively search the given directory for project files.
	 *
	 * @param files the project files we collected so far
	 * @param directory the root directory to search
	 * @param dirsVisited collection of directories we visited so far, to avoid e.g. circular symlinks
	 * @param monitor progress monitor
	 */
	private void collectProjectFiles(Collection<File> files, File directory, Set<String> dirsVisited,
			IProgressMonitor monitor) {
		// Could be a long operation, so offer opportunity to cancel
		if (monitor.isCanceled()) {
			return;
		}

		monitor.subTask(String.format("Checking: %s", directory.getPath()));

		// Skip if not a directory
		File[] contents = directory.listFiles();
		if (contents == null) {
			return;
		}

		// Look in the current directory for project files and collect a list of sub-directories to recurse into
		final List<File> directories = new ArrayList<>();
		for (File file : contents) {
			if (file.isDirectory()) {
				directories.add(file);
			} else if (file.isFile() && getProjectFileNames().contains(file.getName())) {
				files.add(file);
			}
		}

		// Recurse into collected sub-directories
		for (File dir : directories) {
			try {
				String canonicalPath = dir.getCanonicalPath();
				if (!dirsVisited.add(canonicalPath)) {
					// Do not recurse if we already visited
					continue;
				}
			} catch (IOException e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR,
						FrameworkUtil.getBundle(getClass()).getSymbolicName(), e.getMessage(), e));
			}
			collectProjectFiles(files, dir, dirsVisited, monitor);
		}
	}

	@Override
	public boolean shouldBeAnEclipseProject(IContainer container, IProgressMonitor monitor) {
		for (String file : getProjectFileNames()) {
			if (container.getFile(new Path(file)).exists()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Set<IFolder> getFoldersToIgnore(IProject project, IProgressMonitor monitor) {
		// Default to ignoring nothing
		return Set.of();
	}

	@Override
	public boolean canConfigure(IProject project, Set<IPath> ignoredPaths, IProgressMonitor monitor) {
		return shouldBeAnEclipseProject(project, monitor);
	}

	/**
	 * Returns the project generator implementation to be used to configure your project
	 * type. The base Eclipse project will be created for you, the generator just needs to
	 * know how to configure it. The generator will be run during the smart import batch job.
	 *
	 * @param project the project to be configured
	 * @return a project generator
	 */
	protected abstract IGenerator getGenerator(IProject project);

	@Override
	public void configure(IProject project, Set<IPath> ignoredPaths, IProgressMonitor monitor) {
		try {
			IGenerator generator = getGenerator(project);
			generator.generate(monitor);
		} catch (CoreException e) {
			Status status = new Status(e.getStatus().getSeverity(),
					FrameworkUtil.getBundle(getClass()).getSymbolicName(), e.getLocalizedMessage(), e);
			StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.LOG);
		}
	}
}
