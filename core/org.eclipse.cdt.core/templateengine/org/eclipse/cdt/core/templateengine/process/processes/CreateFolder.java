/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Andrew Gvozdev (Quoin Inc.) - Initial API and implementation extracted from CreateSourceFolder.java.
 *******************************************************************************/
package org.eclipse.cdt.core.templateengine.process.processes;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * Creates regular folder in the project.
 * @since 5.1
 */
public class CreateFolder extends ProcessRunner {

	/**
	 * @see org.eclipse.cdt.core.templateengine.process.ProcessRunner#process(org.eclipse.cdt.core.templateengine.TemplateCore, org.eclipse.cdt.core.templateengine.process.ProcessArgument[], java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor)
			throws ProcessFailureException {
		createFolder(args[0].getSimpleValue(), args[1].getSimpleValue(), monitor);
	}

	/**
	 * Creates specified folder in the project recursively.
	 *
	 * @param projectName - name of the project.
	 * @param targetPath - relative path to the new folder.
	 * @param monitor - progress monitor.
	 * @throws ProcessFailureException if there is a problem with creating new folder.
	 */
	public static void createFolder(String projectName, String targetPath, IProgressMonitor monitor)
			throws ProcessFailureException {
		//If the targetPath is an empty string, there will be no folder to create.
		// Also this is not an error. So just return gracefully.
		if (targetPath == null || targetPath.length() == 0) {
			return;
		}

		IProject projectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

		if (!projectHandle.exists()) {
			throw new ProcessFailureException(Messages.getString("CreateSourceFolder.0") + projectName); //$NON-NLS-1$
		}

		IPath path = new Path(targetPath);

		try {
			for (int i = 1; i <= path.segmentCount(); i++) {
				IFolder subfolder = projectHandle.getFolder(path.uptoSegment(i));
				if (!subfolder.exists()) {
					subfolder.create(true, true, monitor);
				}
			}
		} catch (CoreException e) {
			throw new ProcessFailureException(Messages.getString("CreateSourceFolder.1") + e.getMessage(), e); //$NON-NLS-1$
		}

	}
}
