/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.wizards;

import org.eclipse.cdt.make.core.MakefileProjectGenerator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tools.templates.core.IGenerator;
import org.eclipse.tools.templates.ui.TemplateWizard;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

public class NewMakefileProjectWizard extends TemplateWizard {

	private WizardNewProjectCreationPage mainPage;
	private boolean generateSource = true;

	@Override
	public void setContainer(IWizardContainer wizardContainer) {
		super.setContainer(wizardContainer);
		setWindowTitle("New Makefile Project");
	}

	@Override
	public void addPages() {
		mainPage = new WizardNewProjectCreationPage("basicNewProjectPage") { //$NON-NLS-1$
			@Override
			public void createControl(Composite parent) {
				super.createControl(parent);
				Composite comp = (Composite) getControl();
				createWorkingSetGroup(comp, getSelection(), new String[] { "org.eclipse.ui.resourceWorkingSetPage" }); //$NON-NLS-1$

				Composite buttonComp = new Composite(comp, SWT.NONE);
				buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				buttonComp.setLayout(new GridLayout());

				Button genSourceButton = new Button(buttonComp, SWT.CHECK);
				genSourceButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				genSourceButton.setText("Generate Source and Makefile");
				genSourceButton.setSelection(generateSource);
				genSourceButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						generateSource = genSourceButton.getSelection();
					}
				});
				Dialog.applyDialogFont(getControl());
			}
		};
		mainPage.setTitle("New Makefile Project");
		mainPage.setDescription("Specify properties of new Makefile project.");
		this.addPage(mainPage);
	}

	@Override
	protected IGenerator getGenerator() {
		String manifest = generateSource ? "templates/simple/manifest.xml" : null; //$NON-NLS-1$
		MakefileProjectGenerator generator = new MakefileProjectGenerator(manifest);
		generator.setProjectName(mainPage.getProjectName());
		if (!mainPage.useDefaults()) {
			generator.setLocationURI(mainPage.getLocationURI());
		}
		return generator;
	}

}
