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
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.ArrayList;
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

	private static class Collector extends SimpleFileVisitor<Path> {
		private IProgressMonitor monitor;
		private List<PathMatcher> matchers;

		// LinkedHashSet preserves insertion order so it looks nice in the UI
		private Set<File> locations = new LinkedHashSet<>();

		private Collector(List<PathMatcher> matchers, IProgressMonitor monitor) {
			this.monitor = monitor;
			this.matchers = matchers;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			// Could be a long operation, so offer opportunity to cancel
			if (monitor.isCanceled()) {
				return FileVisitResult.TERMINATE;
			}
			monitor.subTask(MessageFormat.format(Messages.ProjectImportConfigurator_Checking, dir));
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			// If file matches any of our patterns, save the location
			Path name = file.getFileName();
			boolean match = matchers.stream().anyMatch(p -> p.matches(name));
			if (match) {
				locations.add(file.getParent().toFile());
			}
			return FileVisitResult.CONTINUE;
		}

		public Set<File> getCollected() {
			return locations;
		}
	}

	/**
	 * The presence of any of these files indicates a directory contains a project
	 * of your type. Whole filenames or glob patterns are acceptable, e.g.
	 * ("build.foo", "foo.*")
	 *
	 * @return a list of filenames and/or glob patterns
	 */
	protected abstract List<String> getProjectFileNames();

	/**
	 * Returns the project generator implementation to be used to configure your project
	 * type. The base Eclipse project will be created for you, the generator just needs to
	 * know how to configure it. The generator will be run during the smart import batch job.
	 *
	 * @param project the project to be configured
	 * @return a project generator
	 */
	protected abstract IGenerator getGenerator(IProject project);

	/**
	 * Utility to create path matchers from glob patterns.
	 */
	private List<PathMatcher> createPathMatchers(List<String> globs) {
		final List<PathMatcher> matchers = new ArrayList<>();
		for (String glob : globs) {
			matchers.add(FileSystems.getDefault().getPathMatcher("glob:" + glob)); //$NON-NLS-1$
		}
		return matchers;
	}

	@Override
	public Set<File> findConfigurableLocations(File root, IProgressMonitor monitor) {
		List<PathMatcher> matchers = createPathMatchers(getProjectFileNames());
		Collector c = new Collector(matchers, monitor);
		try {
			Files.walkFileTree(root.toPath(), c);
		} catch (IOException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR,
					FrameworkUtil.getBundle(getClass()).getSymbolicName(), e.getMessage(), e));
		}
		return c.getCollected();
	}

	@Override
	public boolean shouldBeAnEclipseProject(IContainer container, IProgressMonitor monitor) {
		List<PathMatcher> matchers = createPathMatchers(getProjectFileNames());

		// Only if the location contains a file that matches the one of the given patterns
		File location = container.getLocation().toFile();
		for (File f : location.listFiles()) {
			if (f.isFile() && matchers.stream().anyMatch(p -> p.matches(f.toPath().getFileName()))) {
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
