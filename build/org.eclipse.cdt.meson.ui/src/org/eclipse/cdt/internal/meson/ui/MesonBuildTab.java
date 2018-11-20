/*******************************************************************************
 * Copyright (c) 2017, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Red Hat Inc. - modified for use in Meson build
 *******************************************************************************/
package org.eclipse.cdt.internal.meson.ui;

import java.util.Map;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.internal.meson.core.MesonBuildConfigurationProvider;
import org.eclipse.cdt.launch.ui.corebuild.CommonBuildTab;
import org.eclipse.cdt.meson.core.IMesonConstants;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class MesonBuildTab extends CommonBuildTab {

	private static final String NINJA = "Ninja";
	private Button unixGenButton;
	private Button ninjaGenButton;
	private Text mesonArgsText;
	private Text buildCommandText;
	private Text cleanCommandText;

	@Override
	protected String getBuildConfigProviderId() {
		return MesonBuildConfigurationProvider.ID;
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());
		setControl(comp);

		Control tcControl = createToolchainSelector(comp);
		tcControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Group mesonGroup = new Group(comp, SWT.NONE);
		mesonGroup.setText(Messages.MesonBuildTab_Settings);
		mesonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mesonGroup.setLayout(new GridLayout());

		Label label = new Label(mesonGroup, SWT.NONE);
		label.setText(Messages.MesonBuildTab_Generator);

		Composite genComp = new Composite(mesonGroup, SWT.BORDER);
		genComp.setLayout(new GridLayout(2, true));

		unixGenButton = new Button(genComp, SWT.RADIO);
		unixGenButton.setText(Messages.MesonBuildTab_UnixMakefiles);
		unixGenButton
				.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> updateLaunchConfigurationDialog()));

		ninjaGenButton = new Button(genComp, SWT.RADIO);
		ninjaGenButton.setText(Messages.MesonBuildTab_Ninja);
		ninjaGenButton
				.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> updateLaunchConfigurationDialog()));

		label = new Label(mesonGroup, SWT.NONE);
		label.setText(Messages.MesonBuildTab_MesonArgs);

		mesonArgsText = new Text(mesonGroup, SWT.BORDER);
		mesonArgsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		mesonArgsText.addModifyListener(e -> updateLaunchConfigurationDialog());

		label = new Label(mesonGroup, SWT.NONE);
		label.setText(Messages.MesonBuildTab_BuildCommand);

		buildCommandText = new Text(mesonGroup, SWT.BORDER);
		buildCommandText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		buildCommandText.addModifyListener(e -> updateLaunchConfigurationDialog());

		label = new Label(mesonGroup, SWT.NONE);
		label.setText(Messages.MesonBuildTab_CleanCommand);

		cleanCommandText = new Text(mesonGroup, SWT.BORDER);
		cleanCommandText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		cleanCommandText.addModifyListener(e -> updateLaunchConfigurationDialog());
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// TODO
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);

		ICBuildConfiguration buildConfig = getBuildConfiguration();

		String generator = buildConfig.getProperty(IMesonConstants.MESON_GENERATOR);
		updateGeneratorButtons(generator);

		String mesonArgs = buildConfig.getProperty(IMesonConstants.MESON_ARGUMENTS);
		if (mesonArgs != null) {
			mesonArgsText.setText(mesonArgs);
		} else {
			mesonArgsText.setText(""); //$NON-NLS-1$
		}

		String buildCommand = buildConfig.getProperty(IMesonConstants.BUILD_COMMAND);
		if (buildCommand != null) {
			buildCommandText.setText(buildCommand);
		} else {
			buildCommandText.setText(""); //$NON-NLS-1$
		}

		String cleanCommand = buildConfig.getProperty(IMesonConstants.CLEAN_COMMAND);
		if (cleanCommand != null) {
			cleanCommandText.setText(buildCommand);
		} else {
			cleanCommandText.setText(""); //$NON-NLS-1$
		}
	}

	private void updateGeneratorButtons(String generator) {
		if (generator == null || generator.equals(NINJA)) { //$NON-NLS-1$
			ninjaGenButton.setSelection(true);
		} else {
			unixGenButton.setSelection(true);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);

		ICBuildConfiguration buildConfig = getBuildConfiguration();

		buildConfig.setProperty(IMesonConstants.MESON_GENERATOR,
				ninjaGenButton.getSelection() ? NINJA : "Unix Makefiles"); //$NON-NLS-1$ //$NON-NLS-2$

		String mesonArgs = mesonArgsText.getText().trim();
		if (!mesonArgs.isEmpty()) {
			buildConfig.setProperty(IMesonConstants.MESON_ARGUMENTS, mesonArgs);
		} else {
			buildConfig.removeProperty(IMesonConstants.MESON_ARGUMENTS);
		}

		String buildCommand = buildCommandText.getText().trim();
		if (!buildCommand.isEmpty()) {
			buildConfig.setProperty(IMesonConstants.BUILD_COMMAND, buildCommand);
		} else {
			buildConfig.removeProperty(IMesonConstants.BUILD_COMMAND);
		}

		String cleanCommand = cleanCommandText.getText().trim();
		if (!cleanCommand.isEmpty()) {
			buildConfig.setProperty(IMesonConstants.CLEAN_COMMAND, cleanCommand);
		} else {
			buildConfig.removeProperty(IMesonConstants.CLEAN_COMMAND);
		}
	}

	@Override
	protected void saveProperties(Map<String, String> properties) {
		super.saveProperties(properties);
		properties.put(IMesonConstants.MESON_GENERATOR, ninjaGenButton.getSelection() ? NINJA : "Unix Makefiles"); //$NON-NLS-1$ //$NON-NLS-2$

		properties.put(IMesonConstants.MESON_ARGUMENTS, mesonArgsText.getText().trim());
		properties.put(IMesonConstants.BUILD_COMMAND, buildCommandText.getText().trim());
		properties.put(IMesonConstants.CLEAN_COMMAND, cleanCommandText.getText().trim());
	}

	@Override
	protected void restoreProperties(Map<String, String> properties) {
		super.restoreProperties(properties);

		String gen = properties.get(IMesonConstants.MESON_GENERATOR);
		if (gen != null) {
			switch (gen) {
			case NINJA: //$NON-NLS-1$
				ninjaGenButton.setSelection(true);
				unixGenButton.setSelection(false);
				break;
			case "Unix Makefiles": //$NON-NLS-1$
				ninjaGenButton.setSelection(false);
				unixGenButton.setSelection(true);
				break;
			}
		}

		String mesonArgs = properties.get(IMesonConstants.MESON_ARGUMENTS);
		if (mesonArgs != null) {
			mesonArgsText.setText(mesonArgs);
		} else {
			mesonArgsText.setText(""); //$NON-NLS-1$
		}

		String buildCmd = properties.get(IMesonConstants.BUILD_COMMAND);
		if (buildCmd != null) {
			buildCommandText.setText(buildCmd);
		} else {
			buildCommandText.setText(""); //$NON-NLS-1$
		}

		String cleanCmd = properties.get(IMesonConstants.CLEAN_COMMAND);
		if (cleanCmd != null) {
			cleanCommandText.setText(cleanCmd);
		} else {
			cleanCommandText.setText(""); //$NON-NLS-1$
		}
	}

	@Override
	public String getName() {
		return Messages.MesonBuildTab_Meson;
	}

}
