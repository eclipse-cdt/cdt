/*******************************************************************************
 * Copyright (c) 2016, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Red Hat Inc. - modified for use in Meson build
 *******************************************************************************/
package org.eclipse.cdt.internal.meson.ui;

import org.eclipse.cdt.meson.core.MesonProjectGenerator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tools.templates.core.IGenerator;
import org.eclipse.tools.templates.ui.TemplateWizard;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

public class NewMesonProjectWizard extends TemplateWizard {

	private WizardNewProjectCreationPage mainPage;

	@Override
	public void setContainer(IWizardContainer wizardContainer) {
		super.setContainer(wizardContainer);
		setWindowTitle(Messages.NewMesonProjectWizard_WindowTitle);
	}

	@Override
	public void addPages() {
		mainPage = new WizardNewProjectCreationPage("basicNewProjectPage") { //$NON-NLS-1$
			@Override
			public void createControl(Composite parent) {
				super.createControl(parent);
				createWorkingSetGroup((Composite) getControl(), getSelection(),
						new String[] { "org.eclipse.ui.resourceWorkingSetPage" }); //$NON-NLS-1$
				Dialog.applyDialogFont(getControl());
			}
		};
		mainPage.setTitle(Messages.NewMesonProjectWizard_PageTitle);
		mainPage.setDescription(Messages.NewMesonProjectWizard_Description);
		this.addPage(mainPage);
	}

	@Override
	protected IGenerator getGenerator() {
		MesonProjectGenerator generator = new MesonProjectGenerator("templates/simple/manifest.xml"); //$NON-NLS-1$
		generator.setProjectName(mainPage.getProjectName());
		if (!mainPage.useDefaults()) {
			generator.setLocationURI(mainPage.getLocationURI());
		}
		return generator;
	}

}
