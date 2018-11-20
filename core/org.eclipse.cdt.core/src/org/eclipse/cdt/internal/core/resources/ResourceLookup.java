/*******************************************************************************
 * Copyright (c) 2008, 2013 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Baltasar Belyavsky (Texas Instruments) - [405511] ResourceLookup.selectFile(...) causes deadlocks during project builds
 *******************************************************************************/
package org.eclipse.cdt.internal.core.resources;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * Allows for looking up resources by location or name.
 */
public class ResourceLookup {
	private static ResourceLookupTree lookupTree = new ResourceLookupTree();

	public static void startup() {
		lookupTree.startup();
	}

	public static void shutdown() {
		lookupTree.shutdown();
	}

	/**
	 * Searches for files with the given location suffix.
	 *
	 * At this point the method works for sources and headers (no other content types), only.
	 * This is done to use less memory and can be changed if necessary.
	 * For linked resource files, the name of the link target is relevant.
	 *
	 * @param locationSuffix the suffix to match, always used as relative path.
	 * @param projects the projects to search
	 * @param ignoreCase whether or not to ignore case when comparing the suffix.
	 */
	public static IFile[] findFilesByName(IPath locationSuffix, IProject[] projects, boolean ignoreCase) {
		return lookupTree.findFilesByName(locationSuffix, projects, ignoreCase);
	}

	/**
	 * Uses a lookup-tree that finds resources for locations using the canonical representation
	 * of the path.
	 */
	public static IFile[] findFilesForLocationURI(URI location) {
		return lookupTree.findFilesForLocationURI(location);
	}

	/**
	 * Uses a lookup-tree that finds resources for locations using the canonical representation
	 * of the path. The method does not work for files where the name (last segment) of the
	 * resources differs from the name of the location.
	 */
	public static IFile[] findFilesForLocation(IPath location) {
		return lookupTree.findFilesForLocation(location);
	}

	/**
	 * Uses {@link #findFilesForLocationURI(URI)} and selects the most relevant file
	 * from the result. Files form the first project, from cdt-projects and those on source
	 * roots are preferred, see {@link FileRelevance}.
	 * @param location an URI for the location of the files to search for.
	 * @param preferredProject a project to be preferred over others, or <code>null</code>.
	 * @return a file for the location in one of the given projects, or <code>null</code>.
	 * 			NB the returned IFile may not exist
	 */
	public static IFile selectFileForLocationURI(URI location, IProject preferredProject) {
		return selectFile(findFilesForLocationURI(location), preferredProject, location);
	}

	/**
	 * Uses {@link #findFilesForLocation(IPath)} and selects the most relevant file
	 * from the result. Files form the preferred project, from cdt-projects and those on source
	 * roots are preferred, see {@link FileRelevance}.
	 * @param location a path for the location of the files to search for.
	 * @param preferredProject a project to be preferred over others, or <code>null</code>.
	 * @return a file for the location or <code>null</code>.
	 * 			NB the returned IFile may not exist
	 */
	public static IFile selectFileForLocation(IPath location, IProject preferredProject) {
		return selectFile(findFilesForLocation(location), preferredProject, location);
	}

	/**
	 * Iterates through a list of 'file' resources, and selects the one with the highest "relevance score".
	 *
	 * NOTE: To compute the "relevance scores" this method may cause additional project-descriptions to load.
	 * To avoid the expense of loading additional project-descriptions, we first perform a quick first-pass
	 * through the list of IFiles (which would normally be a very small list), to see if any of them is in
	 * the preferred project. In other words, if we know that the file within the preferred project is the
	 * one that's most relevant, then first try to find it directly - before getting to the more expensive
	 * loop of computing the "relevance scores" for all the files.
	 */
	private static IFile selectFile(IFile[] files, IProject preferredProject, Object originalLocation) {
		if (files.length == 0)
			return null;

		if (files.length == 1)
			return files[0];

		IFile best = null;

		/* FIX for Bug 405511: Try to find the file within the preferred project first - we want to avoid
		 * reaching the next for-loop - that loop is expensive as it might cause the loading of unnecessary
		 * project-descriptions.
		 */
		int filesInPreferredProject = 0;
		if (preferredProject != null) {
			for (IFile file : files) {
				if (file.getProject().equals(preferredProject) && file.isAccessible()) {
					filesInPreferredProject++;
					best = file;
				}
			}
		}
		// One accessible file in preferred project.
		if (filesInPreferredProject == 1)
			return best;

		int bestRelevance = -1;
		for (IFile file : files) {
			if (filesInPreferredProject == 0 || file.getProject().equals(preferredProject)) {
				int relevance = FileRelevance.getRelevance(file, preferredProject,
						PathCanonicalizationStrategy.resolvesSymbolicLinks(), originalLocation);
				if (best == null || relevance > bestRelevance || (relevance == bestRelevance
						&& best.getFullPath().toString().compareTo(file.getFullPath().toString()) > 0)) {
					bestRelevance = relevance;
					best = file;
				}
			}
		}
		return best;
	}

	/**
	 * Sorts files by relevance for CDT, by the criteria listed below. The most relevant files
	 * is listed first.
	 * <br> Accessible files
	 * <br> Files of preferred project
	 * <br> Files of CDT projects
	 * <br> Files on a source root of a CDT project
	 */
	public static void sortFilesByRelevance(IFile[] filesToSort, final IProject preferredProject) {
		Collections.sort(Arrays.asList(filesToSort), new Comparator<IFile>() {
			@Override
			public int compare(IFile f1, IFile f2) {
				int r1 = FileRelevance.getRelevance(f1, preferredProject);
				int r2 = FileRelevance.getRelevance(f2, preferredProject);

				if (r1 > r2)
					return -1;
				if (r1 < r2)
					return 1;

				return f1.getFullPath().toString().compareTo(f2.getFullPath().toString());
			}
		});
	}

	/**
	 * For testing, only.
	 */
	public static void dump() {
		lookupTree.dump();
	}

	/**
	 * For testing, only.
	 */
	public static void unrefNodeMap() {
		lookupTree.unrefNodeMap();
	}

	/**
	 * For testing, only.
	 */
	public static void simulateNodeMapCollection() {
		lookupTree.simulateNodeMapCollection();
	}
}
