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
package org.eclipse.cdt.make.internal.ui;

import java.util.Map;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.StandardBuildConfiguration;
import org.eclipse.cdt.launch.ui.corebuild.CommonBuildTab;
import org.eclipse.cdt.make.core.MakefileBuildConfigurationProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class MakeBuildSettingsTab extends CommonBuildTab {

	private Button projectButton;
	private Button configButton;
	private Text buildCmdText;
	private Text cleanCmdText;

	private boolean defaultProject;

	@Override
	protected String getBuildConfigProviderId() {
		return MakefileBuildConfigurationProvider.ID;
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());
		setControl(comp);

		// Toolchain selector
		Control tcControl = createToolchainSelector(comp);
		tcControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Build Output Group
		Group outputGroup = new Group(comp, SWT.NONE);
		outputGroup.setText(Messages.MakeBuildSettingsTab_BuildOutputLocation);
		outputGroup.setLayout(new GridLayout());
		outputGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		projectButton = new Button(outputGroup, SWT.RADIO);
		projectButton.setText(Messages.MakeBuildSettingsTab_BuildInProjectDir);
		projectButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		configButton = new Button(outputGroup, SWT.RADIO);
		configButton.setText(Messages.MakeBuildSettingsTab_BuildInConfigDir);
		configButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Group cmdGroup = new Group(comp, SWT.NONE);
		cmdGroup.setText(Messages.MakeBuildSettingsTab_BuildCommands);
		cmdGroup.setLayout(new GridLayout(2, false));
		cmdGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label label = new Label(cmdGroup, SWT.NONE);
		label.setText(Messages.MakeBuildSettingsTab_Build);

		buildCmdText = new Text(cmdGroup, SWT.BORDER);
		buildCmdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		label = new Label(cmdGroup, SWT.NONE);
		label.setText(Messages.MakeBuildSettingsTab_Clean);

		cleanCmdText = new Text(cmdGroup, SWT.BORDER);
		cleanCmdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	@Override
	public String getName() {
		return Messages.MakeBuildSettingsTab_Makefile;
	}

	@Override
	protected void restoreProperties(Map<String, String> properties) {
		// TODO Auto-generated method stub
		super.restoreProperties(properties);

		String container = properties.get(StandardBuildConfiguration.BUILD_CONTAINER);
		if (container != null && !container.trim().isEmpty()) {
			IPath containerLoc = new Path(container);
			if (containerLoc.segmentCount() == 1) {
				// TODO what if it's not the project?
				projectButton.setSelection(true);
				defaultProject = true;
			} else {
				configButton.setSelection(true);
				defaultProject = false;
			}
		}

		String buildCommand = properties.get(StandardBuildConfiguration.BUILD_COMMAND);
		if (buildCommand != null && !buildCommand.trim().isEmpty()) {
			buildCmdText.setText(buildCommand);
		}

		String cleanCommand = properties.get(StandardBuildConfiguration.CLEAN_COMMAND);
		if (cleanCommand != null && !cleanCommand.trim().isEmpty()) {
			cleanCmdText.setText(cleanCommand);
		}
	}

	@Override
	protected void saveProperties(Map<String, String> properties) {
		super.saveProperties(properties);

		try {
			ICBuildConfiguration buildConfig = getBuildConfiguration();
			if (buildConfig instanceof StandardBuildConfiguration) {
				StandardBuildConfiguration stdConfig = (StandardBuildConfiguration) buildConfig;
				if (defaultProject && !projectButton.getSelection()) {
					properties.put(StandardBuildConfiguration.BUILD_CONTAINER,
							stdConfig.getDefaultBuildContainer().getFullPath().toString());
				} else if (!defaultProject && projectButton.getSelection()) {
					properties.put(StandardBuildConfiguration.BUILD_CONTAINER,
							stdConfig.getProject().getFullPath().toString());
				}

				String buildCommand = buildCmdText.getText().trim();
				if (!buildCommand.isEmpty()) {
					properties.put(StandardBuildConfiguration.BUILD_COMMAND, buildCommand);
				}

				String cleanCommand = cleanCmdText.getText().trim();
				if (!cleanCommand.isEmpty()) {
					properties.put(StandardBuildConfiguration.CLEAN_COMMAND, cleanCommand);
				}
			}
		} catch (CoreException e) {
			MakeUIPlugin.log(e.getStatus());
		}
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);

		ICBuildConfiguration buildConfig = getBuildConfiguration();
		if (buildConfig == null) {
			return;
		}
		String container = buildConfig.getProperty(StandardBuildConfiguration.BUILD_CONTAINER);
		if (container != null && !container.trim().isEmpty()) {
			IPath containerLoc = new Path(container);
			if (containerLoc.segmentCount() == 1) {
				// TODO what if it's not the project?
				projectButton.setSelection(true);
				defaultProject = true;
			} else {
				configButton.setSelection(true);
				defaultProject = false;
			}
		}

		String buildCommand = buildConfig.getProperty(StandardBuildConfiguration.BUILD_COMMAND);
		if (buildCommand != null && !buildCommand.trim().isEmpty()) {
			buildCmdText.setText(buildCommand);
		}

		String cleanCommand = buildConfig.getProperty(StandardBuildConfiguration.CLEAN_COMMAND);
		if (cleanCommand != null && !cleanCommand.trim().isEmpty()) {
			cleanCmdText.setText(cleanCommand);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);

		try {
			ICBuildConfiguration buildConfig = getBuildConfiguration();
			if (buildConfig instanceof StandardBuildConfiguration) {
				StandardBuildConfiguration stdConfig = (StandardBuildConfiguration) buildConfig;
				if (defaultProject && !projectButton.getSelection()) {
					stdConfig.setBuildContainer(stdConfig.getDefaultBuildContainer());
				} else if (!defaultProject && projectButton.getSelection()) {
					stdConfig.setBuildContainer(stdConfig.getProject());
				}

				String buildCommand = buildCmdText.getText().trim();
				if (!buildCommand.isEmpty()) {
					stdConfig.setBuildCommand(buildCommand.split(" ")); //$NON-NLS-1$
				} else {
					stdConfig.setBuildCommand(null);
				}

				String cleanCommand = cleanCmdText.getText().trim();
				if (!cleanCommand.isEmpty()) {
					stdConfig.setCleanCommand(cleanCommand.split(" ")); //$NON-NLS-1$
				} else {
					stdConfig.setCleanCommand(null);
				}
			}
		} catch (CoreException e) {
			MakeUIPlugin.log(e.getStatus());
		}
	}

}
