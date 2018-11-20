/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
package org.eclipse.cdt.internal.ui.wizards.settingswizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * @since 5.1
 */
public class ProjectSettingsImportWizard extends ProjectSettingsWizard implements IImportWizard {

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.init(workbench, selection);
		setWindowTitle(Messages.ProjectSettingsWizardPage_Import_title);
	}

	@Override
	public ProjectSettingsWizardPage getPage() {
		return ProjectSettingsWizardPage.createImportWizardPage();
	}

}
