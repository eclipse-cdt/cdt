/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal.target;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.target.ILaunchTargetUIManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * The new wizard is responsible for allowing the user to choose which new
 * (nested) wizard to run. The set of available new wizards comes from the new
 * extension point.
 */
public class NewLaunchTargetWizard extends Wizard implements IWorkbenchWizard {
	private NewLaunchTargetWizardSelectionPage mainPage;
	private IWorkbench workbench;
	private final ILaunchTargetUIManager targetUIManager = Activator.getService(ILaunchTargetUIManager.class);

	/**
	 * Create the wizard pages
	 */
	@Override
	public void addPages() {
		mainPage = new NewLaunchTargetWizardSelectionPage(workbench, getWizardDescriptors());
		addPage(mainPage);
	}

	public IWizardDescriptor[] getWizardDescriptors() {
		return targetUIManager.getLaunchTargetWizards();
	}

	/**
	 * Lazily create the wizards pages
	 * @param aWorkbench the workbench
	 * @param currentSelection the current selection
	 */
	@Override
	public void init(IWorkbench aWorkbench,
			IStructuredSelection currentSelection) {
		this.workbench = aWorkbench;
		if (getWindowTitle() == null) {
			setWindowTitle(WorkbenchMessages.NewWizard_title);
		}
		setDefaultPageImageDescriptor(WorkbenchImages
				.getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_NEW_WIZ));
		setNeedsProgressMonitor(true);
	}

	/**
	 * The user has pressed Finish. Instruct self's pages to finish, and answer
	 * a boolean indicating success.
	 *
	 * @return boolean
	 */
	@Override
	public boolean performFinish() {
		//save our selection state
		mainPage.saveWidgetValues();
		// if we're finishing from the main page then perform finish on the selected wizard.
		if (getContainer().getCurrentPage() == mainPage) {
			if (mainPage.canFinishEarly()) {
				IWizard wizard = mainPage.getSelectedNode().getWizard();
				wizard.setContainer(getContainer());
				return wizard.performFinish();
			}
		}
		return true;
	}

	@Override
	public IDialogSettings getDialogSettings() {
		IDialogSettings wizardSettings = super.getDialogSettings();
		if (wizardSettings == null) {
			IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault().getDialogSettings();
			String settingsSection = getClass().getSimpleName();
			wizardSettings = workbenchSettings.getSection(settingsSection);
			if (wizardSettings == null) {
				wizardSettings = workbenchSettings.addNewSection(settingsSection);
			}
			setDialogSettings(wizardSettings);
		}
		return wizardSettings;
	}

	@Override
	public boolean canFinish() {
		// we can finish if the first page is current and the the page can finish early.
		if (getContainer().getCurrentPage() == mainPage) {
			if (mainPage.canFinishEarly()) {
				return true;
			}
		}
		return super.canFinish();
	}
}
