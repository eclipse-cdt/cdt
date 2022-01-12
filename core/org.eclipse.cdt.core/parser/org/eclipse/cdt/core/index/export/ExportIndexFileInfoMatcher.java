/*******************************************************************************
 * Copyright (c) 2013, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Doug Schaefer (QNX) - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.core.index.export;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.resources.FileInfoMatcherDescription;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.filtermatchers.AbstractFileInfoMatcher;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * FileInfoMatcher that will match a given project relative path for a directory we want to exclude.
 *
 * @author dschaefer
 * @since 5.7
 */
public class ExportIndexFileInfoMatcher extends AbstractFileInfoMatcher {

	public static String ID = "org.eclipse.cdt.core.exportIndexFileInfoMatcher"; //$NON-NLS-1$

	private IProject project;
	private IPath excludedFolder;

	public static FileInfoMatcherDescription getDescription(String excludePath) {
		return new FileInfoMatcherDescription(ID, excludePath);
	}

	public ExportIndexFileInfoMatcher() {
	}

	@Override
	public boolean matches(IContainer parent, IFileInfo fileInfo) throws CoreException {
		if (excludedFolder == null || project == null)
			return false;

		if (!project.equals(parent.getProject()))
			return false;

		// Remove the project and the linked folder from the path
		IPath testPath = parent.getFullPath().removeFirstSegments(2).append(fileInfo.getName());
		boolean matches = excludedFolder.isPrefixOf(testPath);
		if (matches)
			System.out.println("Filtering: " + testPath); //$NON-NLS-1$
		return matches;
	}

	@Override
	public void initialize(IProject project, Object arguments) throws CoreException {
		this.project = project;
		if (arguments instanceof String)
			excludedFolder = new Path((String) arguments);
	}

}
