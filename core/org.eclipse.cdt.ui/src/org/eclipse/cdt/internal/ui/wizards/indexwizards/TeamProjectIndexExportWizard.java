/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.wizards.indexwizards;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.CPluginImages;

public class TeamProjectIndexExportWizard extends Wizard implements IExportWizard {
	private static final String DIALOG_SETTINGS_SECTION = "TeamProjectIndexExportWizard"; //$NON-NLS-1$
	private TeamProjectIndexExportWizardPage fMainPage;
	private IStructuredSelection fSelection;

	public TeamProjectIndexExportWizard() {
        IDialogSettings workbenchSettings = CUIPlugin.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection(DIALOG_SETTINGS_SECTION);
        if (section == null) {
			section = workbenchSettings.addNewSection(DIALOG_SETTINGS_SECTION);
		}
        setDialogSettings(section);
	}

    @Override
	public void addPages() {
        super.addPages();
        fMainPage = new TeamProjectIndexExportWizardPage(fSelection);
        addPage(fMainPage);
    }

	@Override
	public boolean performFinish() {
        return fMainPage.finish();
    }

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
        fSelection= selection;
        setWindowTitle(Messages.TeamProjectIndexExportWizard_title);
        setDefaultPageImageDescriptor(CPluginImages.DESC_WIZBAN_EXPORTINDEX);
        setNeedsProgressMonitor(true);
	}

}
