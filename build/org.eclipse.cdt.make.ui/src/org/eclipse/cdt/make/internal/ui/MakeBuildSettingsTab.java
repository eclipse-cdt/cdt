/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

public class MakeBuildSettingsTab extends CommonBuildTab {

	private Button projectButton;
	private Button configButton;

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
		Group group = new Group(comp, SWT.NONE);
		group.setText("Build Output Location");
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		projectButton = new Button(group, SWT.RADIO);
		projectButton.setText("Build in project directory");
		projectButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		configButton = new Button(group, SWT.RADIO);
		configButton.setText("Build in configuration specific folder");
		configButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	@Override
	public String getName() {
		return "Makefile";
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
			}
		} catch (CoreException e) {
			MakeUIPlugin.log(e.getStatus());
		}
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);

		ICBuildConfiguration buildConfig = getBuildConfiguration();
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
			}
		} catch (CoreException e) {
			MakeUIPlugin.log(e.getStatus());
		}
	}

}
