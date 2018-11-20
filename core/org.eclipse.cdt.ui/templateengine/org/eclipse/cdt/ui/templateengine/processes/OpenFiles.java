/*******************************************************************************
 * Copyright (c) 2010, 2018 Marc-Andre Laperle and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Marc-Andre Laperle - Initial API and implementation
 * Mohamed Hussein (Mentor Graphics) - Bug 389392 fix refresh when open existing editor
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine.processes;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * This process opens files in the editor
 *
 * @since 5.2
 */
public class OpenFiles extends ProcessRunner {

	/**
	 * This method opens a list of files in the editor
	 */
	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor)
			throws ProcessFailureException {
		ProcessArgument[][] files = args[1].getComplexArrayValue();
		for (ProcessArgument[] file : files) {
			String fileTargetPath = file[0].getSimpleValue();
			String projectName = args[0].getSimpleValue();
			IProject projectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			IFile iFile = projectHandle.getFile(fileTargetPath);
			if (iFile.exists()) {
				// Only open files if they are not open to avoid refresh problem if files have been modified multiple times
				if (!isOpen(iFile)) {
					try {
						IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), iFile);
					} catch (PartInitException e) {
						throw new ProcessFailureException(Messages.OpenFiles_CannotOpen_error + fileTargetPath);
					}
				}
			} else {
				throw new ProcessFailureException(Messages.OpenFiles_FileNotExist_error + fileTargetPath);
			}
		}
	}

	private boolean isOpen(IFile file) {
		IEditorReference[] editorReferences = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getEditorReferences();
		if (editorReferences != null) {
			IEditorInput editorInput;
			for (IEditorReference editorReference : editorReferences) {
				try {
					editorInput = editorReference.getEditorInput();
					if (editorInput instanceof IFileEditorInput
							&& file.equals(((IFileEditorInput) editorInput).getFile())) {
						return true;
					}
				} catch (PartInitException e) {
				}
			}
		}

		return false;
	}

}
