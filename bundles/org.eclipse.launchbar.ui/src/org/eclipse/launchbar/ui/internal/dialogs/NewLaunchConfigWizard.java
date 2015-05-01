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
package org.eclipse.launchbar.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.internal.Messages;

public class NewLaunchConfigWizard extends Wizard implements ILaunchConfigurationListener {

	NewLaunchConfigModePage modePage = new NewLaunchConfigModePage();
	NewLaunchConfigTypePage typePage = new NewLaunchConfigTypePage();
	NewLaunchConfigEditPage editPage = new NewLaunchConfigEditPage();
	
	private List<ILaunchConfiguration> configsToDelete = new ArrayList<>();

	public NewLaunchConfigWizard() {
		setWindowTitle(Messages.NewLaunchConfigWizard_0);
		initListeners();
	}
	
	@Override
	public void addPages() {
		addPage(modePage);
		addPage(typePage);
		addPage(editPage);
	}
	
	public ILaunchConfigurationWorkingCopy getWorkingCopy() {
		return editPage.workingCopy;
	}

	public ILaunchMode getLaunchMode() {
		String initMode = modePage.selectedGroup.getMode();
		return DebugPlugin.getDefault().getLaunchManager().getLaunchMode(initMode);		
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

	private void initListeners() {
		// while the wizard is open, some ill behaved launch config tabs save the working copy.
		// We need to make sure those saves are deleted when the dialog is finished.
		// We also need to turn off listening in the tool bar manager so that we don't treat these
		// as real launch configs.
		DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(this);
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
