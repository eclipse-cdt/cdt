/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.filewizard;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * A generic new file wizard with support for file templates.
 * Based on {@link org.eclipse.ui.wizards.newresource.BasicNewFileResourceWizard BasicNewFileResourceWizard}.
 *
 * @since 5.0
 */
public class NewFileFromTemplateWizard extends BasicNewResourceWizard {
	private WizardNewFileCreationPage mainPage;

	/**
	 * Creates a wizard for creating a new file resource in the workspace.
	 */
	public NewFileFromTemplateWizard() {
		super();
	}

	/*
	 * Method declared on IWizard.
	 */
	@Override
	public void addPages() {
		super.addPages();
		mainPage = new WizardNewFileFromTemplateCreationPage("newFilePage1", getSelection());//$NON-NLS-1$
		mainPage.setTitle(NewFileWizardMessages.NewFileFromTemplateWizard_pageTitle);
		mainPage.setDescription(NewFileWizardMessages.NewFileFromTemplateWizard_description);
		addPage(mainPage);
	}

	/*
	 * Method declared on IWorkbenchWizard.
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		super.init(workbench, currentSelection);
		setWindowTitle(NewFileWizardMessages.NewFileFromTemplateWizard_shellTitle);
		setNeedsProgressMonitor(true);
	}

	/*
	 * Method declared on BasicNewResourceWizard.
	 */
	@Override
	protected void initializeDefaultPageImageDescriptor() {
		ImageDescriptor desc = CPluginImages.DESC_WIZBAN_NEW_FILE;
		setDefaultPageImageDescriptor(desc);
	}

	/*
	 * Method declared on IWizard.
	 */
	@Override
	public boolean performFinish() {
		IFile file = mainPage.createNewFile();
		if (file == null) {
			return false;
		}

		selectAndReveal(file);

		// Open editor on new file.
		IWorkbenchWindow dw = getWorkbench().getActiveWorkbenchWindow();
		try {
			if (dw != null) {
				IWorkbenchPage page = dw.getActivePage();
				if (page != null) {
					IDE.openEditor(page, file, true);
				}
			}
		} catch (PartInitException e) {
			openError(getShell(), NewFileWizardMessages.NewFileFromTemplateWizard_errorMessage, e.getMessage(), e);
		}

		return true;
	}

	/**
	 * Open an error style dialog for PartInitException by
	 * including any extra information from the nested
	 * CoreException if present.
	 */
	public static void openError(Shell parent, String title, String message, PartInitException exception) {
		// Check for a nested CoreException
		CoreException nestedException = null;
		IStatus status = exception.getStatus();
		if (status != null && status.getException() instanceof CoreException) {
			nestedException = (CoreException) status.getException();
		}

		if (nestedException != null) {
			// Open an error dialog and include the extra
			// status information from the nested CoreException
			ErrorDialog.openError(parent, title, message, nestedException.getStatus());
		} else {
			// Open a regular error dialog since there is no
			// extra information to display
			MessageDialog.openError(parent, title, message);
		}
	}
}
