/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.internal.Messages;
import org.eclipse.launchbar.ui.internal.dialogs.NewLaunchConfigEditPage;
import org.eclipse.launchbar.ui.internal.dialogs.NewLaunchConfigTypePage2;

public class NewLaunchConfigWizard extends Wizard implements ILaunchConfigurationListener {

	private NewLaunchConfigTypePage2 typePage;
	private NewLaunchConfigEditPage editPage;

	private List<ILaunchConfiguration> configsToDelete = new ArrayList<>();

	public NewLaunchConfigWizard() {
		editPage = new NewLaunchConfigEditPage();
		typePage = new NewLaunchConfigTypePage2(editPage);

		setWindowTitle(Messages.NewLaunchConfigWizard_0);

		// while the wizard is open, some ill behaved launch config tabs save the working copy.
		// We need to make sure those saves are deleted when the dialog is finished.
		// We also need to turn off listening in the tool bar manager so that we don't treat these
		// as real launch configs.
		DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(this);
	}

	@Override
	public void setContainer(IWizardContainer wizardContainer) {
		super.setContainer(wizardContainer);
		
		if (wizardContainer != null) {
			// Edit page wants to know when it's about to change to itself
			((WizardDialog) wizardContainer).addPageChangingListener(editPage);
		}
	}

	@Override
	public void addPages() {
		addPage(typePage);
		addPage(editPage);
	}

	@Override
	public boolean canFinish() {
		if (getPage(editPage.getName()) == null) {
			return false;
		}
		return super.canFinish();
	}

	public ILaunchConfigurationWorkingCopy getWorkingCopy() {
		return editPage.getWorkingCopy();
	}

	public ILaunchMode getLaunchMode() {
		// TODO we want to make sure the newly created config has the right mode
		// selected in the launch bar
		return null;
	}

	@Override
	public boolean performFinish() {
		cleanUpConfigs();
		return editPage.performFinish();
	}

	@Override
	public boolean performCancel() {
		cleanUpConfigs();
		return super.performCancel();
	}

	void cleanUpConfigs() {
		DebugPlugin.getDefault().getLaunchManager().removeLaunchConfigurationListener(this);
		for (ILaunchConfiguration config : configsToDelete) {
			try {
				config.delete();
			} catch (CoreException e) {
				Activator.log(e);
			}
		}
	}

	@Override
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		if (!configuration.isWorkingCopy())
			configsToDelete.add(configuration);
	}

	@Override
	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
		// Nothing to do
	}

	@Override
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
		if (!configuration.isWorkingCopy())
			configsToDelete.remove(configuration);
	}

}
