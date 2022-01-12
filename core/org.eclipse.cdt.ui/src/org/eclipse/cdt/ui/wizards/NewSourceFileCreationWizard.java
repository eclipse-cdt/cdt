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
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.wizards.filewizard.AbstractFileCreationWizard;
import org.eclipse.cdt.internal.ui.wizards.filewizard.NewFileWizardMessages;
import org.eclipse.cdt.internal.ui.wizards.filewizard.NewSourceFileCreationWizardPage;
import org.eclipse.cdt.ui.CUIPlugin;

public class NewSourceFileCreationWizard extends AbstractFileCreationWizard {

	public NewSourceFileCreationWizard() {
		super();
		setDefaultPageImageDescriptor(CPluginImages.DESC_WIZBAN_NEW_SOURCEFILE);
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
		setWindowTitle(NewFileWizardMessages.NewSourceFileCreationWizard_title);
	}

	/*
	 * @see Wizard#createPages
	 */
	@Override
	public void addPages() {
		super.addPages();
		fPage = new NewSourceFileCreationWizardPage();
		addPage(fPage);
		fPage.init(getSelection());
	}
}
