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
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.cdt.internal.ui.wizards.folderwizard.NewFolderWizardMessages;
import org.eclipse.cdt.internal.ui.wizards.folderwizard.NewSourceFolderWizardPage;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class NewSourceFolderCreationWizard extends NewElementWizard {
	private NewSourceFolderWizardPage fPage;

	public NewSourceFolderCreationWizard() {
		super();
		setDefaultPageImageDescriptor(CPluginImages.DESC_WIZBAN_NEWSRCFOLDR);
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
		setWindowTitle(NewFolderWizardMessages.NewSourceFolderCreationWizard_title);
	}

	@Override
	public void addPages() {
		super.addPages();
		fPage = new NewSourceFolderWizardPage();
		addPage(fPage);
		fPage.init(getSelection());
	}

	@Override
	protected void finishPage(IProgressMonitor monitor) throws CoreException {
		fPage.createSourceRoot(monitor); // Use the full progress monitor.
	}

	@Override
	public boolean performFinish() {
		boolean res = super.performFinish();
		if (res) {
			selectAndReveal(fPage.getCorrespondingResource());
		}
		return res;
	}
}
