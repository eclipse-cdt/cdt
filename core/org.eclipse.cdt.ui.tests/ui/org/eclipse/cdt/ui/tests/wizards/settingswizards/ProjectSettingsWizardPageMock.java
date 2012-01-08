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
package org.eclipse.cdt.ui.tests.wizards.settingswizards;

import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;

import org.eclipse.cdt.internal.ui.wizards.settingswizards.IProjectSettingsWizardPage;
import org.eclipse.cdt.internal.ui.wizards.settingswizards.ISettingsProcessor;

public class ProjectSettingsWizardPageMock implements IProjectSettingsWizardPage {

	private String path;
	private List<ISettingsProcessor> selectedSettingsProcessors;
	private ICConfigurationDescription selectedConfiguration;
	private List<ISettingsProcessor> settingsProcessors;
	
	public void setDestinationFilePath(String path) {
		this.path = path;
	}
	
	@Override
	public String getDestinationFilePath() {
		return path;
	}
	
	public void setSelectedConfiguration(ICConfigurationDescription config) {
		this.selectedConfiguration = config;
	}

	@Override
	public ICConfigurationDescription getSelectedConfiguration() {
		return selectedConfiguration;
	}

	public void setSelectedSettingsProcessors(List<ISettingsProcessor> processors) {
		this.selectedSettingsProcessors = processors;
	}
	
	@Override
	public List<ISettingsProcessor> getSelectedSettingsProcessors() {
		return selectedSettingsProcessors;
	}

	public void setSettingsProcessors(List<ISettingsProcessor> processors) {
		this.settingsProcessors = processors;
	}
	
	@Override
	public List<ISettingsProcessor> getSettingsProcessors() {
		return settingsProcessors;
	}
		
	@Override
	public void setDisplayedSettingsProcessors( List<ISettingsProcessor> processors) {
		// do nothing
	}

	@Override
	public void setMessage(String message, int flag) {
		// do nothing
	}

	@Override
	public void showErrorDialog(String dialogTitle, String message) {
		// do nothing
	}

}
