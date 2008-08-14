/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.settingswizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * @since 5.1
 */
public class ProjectSettingsExportWizard extends ProjectSettingsWizard implements IExportWizard {


	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.init(workbench, selection);
		setWindowTitle(Messages.ProjectSettingsWizardPage_Export_title); 
	}

	@Override
	public ProjectSettingsWizardPage getPage() {
		return ProjectSettingsWizardPage.createExportWizardPage();
	}
	

}
