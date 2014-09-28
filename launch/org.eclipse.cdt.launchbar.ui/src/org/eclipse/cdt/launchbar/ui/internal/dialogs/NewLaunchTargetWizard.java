/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.cdt.launchbar.ui.internal.dialogs;

import org.eclipse.cdt.launchbar.ui.internal.LaunchBarUIManager;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.PlatformUI;

public class NewLaunchTargetWizard extends Wizard {

	private NewLaunchTargetTypePage typePage = new NewLaunchTargetTypePage();
	final LaunchBarUIManager uiManager;
	private INewWizard selectedWizard;
	
	public NewLaunchTargetWizard(LaunchBarUIManager uiManager) {
		this.uiManager = uiManager;
		setForcePreviousAndNextButtons(true);
	}

	@Override
	public void addPages() {
		addPage(typePage);
	}
	
	@Override
	public boolean performFinish() {
		if (selectedWizard != null)
			return selectedWizard.performFinish();
		return true;
	}

	@Override
	public boolean performCancel() {
		if (selectedWizard != null)
			return selectedWizard.performCancel();
		return true;
	}

	@Override
	public boolean canFinish() {
		if (getContainer().getCurrentPage() == typePage)
			return false;
		if (selectedWizard != null)
			return selectedWizard.canFinish();
		return false;
	}

	void wizardSelected(INewWizard selectedWizard) {
		this.selectedWizard = selectedWizard;
		selectedWizard.init(PlatformUI.getWorkbench(), null);
		selectedWizard.addPages();
		IWizardPage[] pages = selectedWizard.getPages();
		for (IWizardPage page : pages) {
			addPage(page);
		}
	}

}
