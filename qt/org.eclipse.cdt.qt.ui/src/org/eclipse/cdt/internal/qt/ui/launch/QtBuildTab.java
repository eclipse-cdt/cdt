/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.ui.launch;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.debug.core.launch.CoreBuildLaunchConfigDelegate;
import org.eclipse.cdt.internal.qt.core.build.QtBuildConfiguration;
import org.eclipse.cdt.internal.qt.ui.Activator;
import org.eclipse.cdt.internal.qt.ui.Messages;
import org.eclipse.cdt.launch.ui.corebuild.CommonBuildTab;
import org.eclipse.cdt.qt.core.IQtInstall;
import org.eclipse.cdt.qt.core.IQtInstallManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class QtBuildTab extends CommonBuildTab {

	Combo qmakeCombo;
	Text qmakeArgsText;
	Text buildCmdText;

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		setControl(comp);

		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.QtBuildTab_qmakeCommand);

		qmakeCombo = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
		qmakeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		label = new Label(comp, SWT.NONE);
		label.setText(Messages.QtBuildTab_qmakeArgs);

		qmakeArgsText = new Text(comp, SWT.BORDER);
		qmakeArgsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		label = new Label(comp, SWT.NONE);
		label.setText(Messages.QtBuildTab_buildCommand);

		buildCmdText = new Text(comp, SWT.BORDER);
		buildCmdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		try {
			String mode = getLaunchConfigurationDialog().getMode();
			configuration.setAttribute(CoreBuildLaunchConfigDelegate.getBuildAttributeName(mode),
					getBuildConfiguration(configuration).getDefaultProperties());
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	private Map<String, String> getProperties(ILaunchConfiguration configuration) throws CoreException {
		String mode = getLaunchConfigurationDialog().getMode();
		Map<String, String> properties = configuration
				.getAttribute(CoreBuildLaunchConfigDelegate.getBuildAttributeName(mode), new HashMap<>());
		if (properties.isEmpty()) {
			properties = getBuildConfiguration(configuration).getProperties();
		}

		return properties;
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			Map<String, String> properties = getProperties(configuration);

			// qmake command
			IToolChainManager tcManager = Activator.getService(IToolChainManager.class);
			IQtInstallManager qtManager = Activator.getService(IQtInstallManager.class);
			ILaunchTarget target = getLaunchTarget();

			String qmakeCmd = properties.get(QtBuildConfiguration.QMAKE_COMMAND);
			qmakeCombo.removeAll();
			Collection<IToolChain> toolChains = tcManager.getToolChainsMatching(target.getAttributes());
			int select = -1;
			for (IQtInstall qtInstall : qtManager.getInstalls()) {
				for (IToolChain toolChain : toolChains) {
					if (qtManager.supports(qtInstall, toolChain)) {
						qmakeCombo.add(qtInstall.getQmakePath().toString());
						if (qmakeCmd != null && qmakeCmd.equals(qtInstall.getQmakePath().toString())) {
							select = qmakeCombo.getItemCount() - 1;
						}
						break;
					}
				}
			}

			if (select != -1) {
				qmakeCombo.select(select);
			}

			// qmake args
			String qmakeArgs = properties.get(QtBuildConfiguration.QMAKE_ARGS);
			if (qmakeArgs != null) {
				qmakeArgsText.setText(qmakeArgs);
			}

			// build command
			String buildCommand = properties.get(QtBuildConfiguration.BUILD_COMMAND);
			if (buildCommand != null) {
				buildCmdText.setText(buildCommand);
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		try {
			Map<String, String> properties = new HashMap<>(getProperties(configuration));
			properties.put(QtBuildConfiguration.QMAKE_COMMAND, qmakeCombo.getItem(qmakeCombo.getSelectionIndex()));
			properties.put(QtBuildConfiguration.QMAKE_ARGS, qmakeArgsText.getText().trim());
			properties.put(QtBuildConfiguration.BUILD_COMMAND, buildCmdText.getText().trim());

			String mode = getLaunchBarLaunchConfigDialog().getMode();
			configuration.setAttribute(CoreBuildLaunchConfigDelegate.getBuildAttributeName(mode), properties);
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	@Override
	public String getName() {
		return Messages.QtBuildTab_Name;
	}

}
