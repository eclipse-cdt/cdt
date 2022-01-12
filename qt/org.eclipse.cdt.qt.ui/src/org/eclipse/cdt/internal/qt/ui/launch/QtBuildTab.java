/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.ui.launch;

import java.util.Collection;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
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

	private Combo qmakeCombo;
	private Text qmakeArgsText;
	private Text buildCmdText;

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
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			ICBuildConfiguration buildConfig = getBuildConfiguration();
			if (buildConfig == null) {
				return;
			}

			// qmake command
			IToolChainManager tcManager = Activator.getService(IToolChainManager.class);
			IQtInstallManager qtManager = Activator.getService(IQtInstallManager.class);
			ILaunchTarget target = getLaunchTarget();

			String qmakeCmd = buildConfig.getProperty(QtBuildConfiguration.QMAKE_COMMAND);
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
			String qmakeArgs = buildConfig.getProperty(QtBuildConfiguration.QMAKE_ARGS);
			if (qmakeArgs != null) {
				qmakeArgsText.setText(qmakeArgs);
			}

			// build command
			String buildCommand = buildConfig.getProperty(QtBuildConfiguration.BUILD_COMMAND);
			if (buildCommand != null) {
				buildCmdText.setText(buildCommand);
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		ICBuildConfiguration buildConfig = getBuildConfiguration();
		buildConfig.removeProperty(QtBuildConfiguration.QMAKE_ARGS);
		buildConfig.removeProperty(QtBuildConfiguration.BUILD_COMMAND);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		ICBuildConfiguration buildConfig = getBuildConfiguration();
		buildConfig.setProperty(QtBuildConfiguration.QMAKE_COMMAND, qmakeCombo.getItem(qmakeCombo.getSelectionIndex()));

		String qmakeArgs = qmakeArgsText.getText().trim();
		if (qmakeArgs.isEmpty()) {
			buildConfig.removeProperty(QtBuildConfiguration.QMAKE_ARGS);
		} else {
			buildConfig.setProperty(QtBuildConfiguration.QMAKE_ARGS, qmakeArgs);
		}

		String buildCmd = buildCmdText.getText().trim();
		if (buildCmd.isEmpty()) {
			buildConfig.removeProperty(QtBuildConfiguration.BUILD_COMMAND);
		} else {
			buildConfig.setProperty(QtBuildConfiguration.BUILD_COMMAND, buildCmd);
		}
	}

	@Override
	public String getName() {
		return Messages.QtBuildTab_Name;
	}

}
