/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.templateengine.process.processes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.TemplateEngineHelper;
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
import org.eclipse.core.runtime.IStatus;

/*
 * Appends a file to an existing file if present. If not, create the file
 */
public class AppendCreate extends ProcessRunner {
	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor)
			throws ProcessFailureException {
		String projectName = args[0].getSimpleValue();
		IProject projectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		ProcessArgument[][] files = args[1].getComplexArrayValue();
		for (int i = 0; i < files.length; i++) {
			ProcessArgument[] file = files[i];
			String sourcePath = file[0].getSimpleValue();
			String targetPath = file[1].getSimpleValue();
			boolean replaceable = file[2].getSimpleValue().equals("true"); //$NON-NLS-1$

			URL sourceURL;
			try {
				sourceURL = TemplateEngineHelper.getTemplateResourceURLRelativeToTemplate(template, sourcePath);
				if (sourceURL == null) {
					throw new ProcessFailureException(getProcessMessage(processId, IStatus.ERROR,
							Messages.getString("AppendCreate.1") + sourcePath)); //$NON-NLS-1$
				}
			} catch (IOException e1) {
				throw new ProcessFailureException(Messages.getString("AppendCreate.2") + sourcePath); //$NON-NLS-1$
			}
			String fileContents;
			try {
				fileContents = ProcessHelper.readFromFile(sourceURL);
			} catch (IOException e1) {
				throw new ProcessFailureException(Messages.getString("AppendCreate.3") + sourcePath); //$NON-NLS-1$
			}
			if (replaceable) {
				fileContents = ProcessHelper.getValueAfterExpandingMacros(fileContents,
						ProcessHelper.getReplaceKeys(fileContents), template.getValueStore());
			}
			try {
				// Check whether the file exists
				IFile iFile = projectHandle.getFile(targetPath);
				if (!iFile.getParent().exists()) {
					ProcessHelper.mkdirs(projectHandle,
							projectHandle.getFolder(iFile.getParent().getProjectRelativePath()));
				}
				InputStream contents = new ByteArrayInputStream(fileContents.getBytes());
				if (!iFile.exists()) {
					// Create the file
					iFile.create(contents, true, null);
					iFile.refreshLocal(IResource.DEPTH_ONE, null);

				} else {
					// Append the file keeping the history
					iFile.appendContents(contents, true, true, null);
				}
				// Update the project
				projectHandle.refreshLocal(IResource.DEPTH_INFINITE, null);

			} catch (CoreException e) {
				throw new ProcessFailureException(Messages.getString("AppendCreate.4"), e); //$NON-NLS-1$
			}
		}
	}
}
