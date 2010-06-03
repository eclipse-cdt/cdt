/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.templateengine.process.processes;

import java.io.File;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessHelper;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;


/**
 * Adds a Link to the Project.
 */
public class AddLink extends ProcessRunner {

	/**
	 * This method Adds a Link to the Project.
	 */
	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor) throws ProcessFailureException {
		String projectName = args[0].getSimpleValue();
		String fileSourcePath = args[1].getSimpleValue();
		String targetPath = args[2].getSimpleValue();

		File sourceFile = new java.io.File(fileSourcePath);
		
		IProject projectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

		try {
			IFile file = projectHandle.getFile(targetPath);
			if (!file.getParent().exists()) {
				ProcessHelper.mkdirs(projectHandle, projectHandle.getFolder(file.getParent().getProjectRelativePath()));
			}
			file.createLink(Path.fromOSString(sourceFile.getAbsolutePath()), IResource.ALLOW_MISSING_LOCAL | IResource.BACKGROUND_REFRESH, null);
			file.refreshLocal(IResource.DEPTH_ONE, null);
			projectHandle.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			throw new ProcessFailureException(Messages.getString("AddLink.0") + e.getMessage(), e); //$NON-NLS-1$
		}
	}
}
