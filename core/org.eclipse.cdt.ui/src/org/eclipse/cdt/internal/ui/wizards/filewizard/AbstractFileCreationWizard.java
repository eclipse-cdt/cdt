/*******************************************************************************
 * Copyright (c) 2004, 2008 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.filewizard;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public abstract class AbstractFileCreationWizard extends NewElementWizard {
	protected AbstractFileCreationWizardPage fPage;

	public AbstractFileCreationWizard() {
		super();
		setDefaultPageImageDescriptor(CPluginImages.DESC_WIZBAN_NEW_FILE);
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
		setWindowTitle(NewFileWizardMessages.AbstractFileCreationWizard_title);
	}

	@Override
	protected boolean canRunForked() {
		return true;
	}

	@Override
	protected void finishPage(IProgressMonitor monitor) throws CoreException {
		fPage.createFile(monitor); // Use the full progress monitor.
	}

	@Override
	public boolean performFinish() {
		boolean result = super.performFinish();
		if (result) {
			//TODO need prefs option for opening editor
			boolean openInEditor = true;

			ITranslationUnit headerTU = fPage.getCreatedFileTU();
			if (headerTU != null) {
				IResource resource = headerTU.getResource();
				selectAndReveal(resource);
				if (openInEditor) {
					openResource((IFile) resource);
				}
			}
		}
		return result;
	}
}
