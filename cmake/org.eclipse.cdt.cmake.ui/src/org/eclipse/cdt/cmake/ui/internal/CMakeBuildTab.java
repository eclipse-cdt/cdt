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
package org.eclipse.cdt.cmake.ui.internal;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.cdt.cmake.core.CMakeBuildConfiguration;
import org.eclipse.cdt.cmake.core.CMakeBuildConfigurationProvider;
import org.eclipse.cdt.cmake.core.properties.CMakeGenerator;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.launch.ui.corebuild.CommonBuildTab;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class CMakeBuildTab extends CommonBuildTab {

	private static final String[] buildTypes = { "Debug", "Release", "RelWithDebInfo", "MinSizeRel" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	private Button useDefaultCmakeSettings;
	private Combo generatorCombo;
	private Text cmakeArgsText;
	private Text buildCommandText;
	private Text allTargetText;
	private Text cleanTargetText;
	private Label generatorLabel;
	private Label cmakeArgsLabel;
	private Label buildCommandLabel;
	private Label allTargetLabel;
	private Label cleanTargetLabel;
	private Combo buildTypeCombo;
	private Label buildTypeLabel;
	private Label usedForLaunchModeLabel;

	@Override
	protected String getBuildConfigProviderId() {
		return CMakeBuildConfigurationProvider.ID;
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());
		setControl(comp);

		Control tcControl = createToolchainSelector(comp);
		tcControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Group cmakeGroup = new Group(comp, SWT.NONE);
		cmakeGroup.setText(Messages.CMakeBuildTab_Settings);
		cmakeGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		cmakeGroup.setLayout(new GridLayout());

		useDefaultCmakeSettings = new Button(cmakeGroup, SWT.CHECK);
		useDefaultCmakeSettings.setText(Messages.CMakeBuildTab_useDefaultCmakeSettings);
		useDefaultCmakeSettings.setToolTipText(Messages.CMakeBuildTab_useDefaultCmakeSettingsTip);
		useDefaultCmakeSettings.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showDefaultsInUi();
				updateEnablement();
				updateLaunchConfigurationDialog();
			}

		});

		generatorLabel = new Label(cmakeGroup, SWT.NONE);
		generatorLabel.setText(Messages.CMakeBuildTab_Generator);
		generatorLabel.setToolTipText(Messages.CMakeBuildTab_GeneratorTooltip);
		CMakeGenerator[] generators = CMakeGenerator.values();
		String[] generatorNames = Arrays.stream(generators).map(CMakeGenerator::getCMakeName).toArray(String[]::new);
		generatorCombo = new Combo(cmakeGroup, SWT.DROP_DOWN);
		generatorCombo.setToolTipText(Messages.CMakeBuildTab_GeneratorComboTooltip);
		generatorCombo.setItems(generatorNames);
		generatorCombo.select(0);
		generatorCombo.addModifyListener(e -> updateLaunchConfigurationDialog());
		generatorCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		cmakeArgsLabel = new Label(cmakeGroup, SWT.NONE);
		cmakeArgsLabel.setText(Messages.CMakeBuildTab_CMakeArgs);
		cmakeArgsLabel.setToolTipText(Messages.CMakeBuildTab_cmakeArgsTooltip);

		cmakeArgsText = new Text(cmakeGroup, SWT.BORDER);
		cmakeArgsText.setToolTipText(Messages.CMakeBuildTab_cmakeArgsTooltip);
		cmakeArgsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		cmakeArgsText.addModifyListener(e -> updateLaunchConfigurationDialog());

		buildCommandLabel = new Label(cmakeGroup, SWT.NONE);
		buildCommandLabel.setText(Messages.CMakeBuildTab_BuildCommand);
		buildCommandLabel.setToolTipText(Messages.CMakeBuildTab_buildCommandTooltip);

		buildCommandText = new Text(cmakeGroup, SWT.BORDER);
		buildCommandText.setToolTipText(Messages.CMakeBuildTab_buildCommandTooltip);
		buildCommandText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		buildCommandText.addModifyListener(e -> updateLaunchConfigurationDialog());

		allTargetLabel = new Label(cmakeGroup, SWT.NONE);
		allTargetLabel.setText(Messages.CMakeBuildTab_AllTarget);
		allTargetLabel.setToolTipText(Messages.CMakeBuildTab_allTargetTooltip);

		allTargetText = new Text(cmakeGroup, SWT.BORDER);
		allTargetText.setToolTipText(Messages.CMakeBuildTab_allTargetTooltip);
		allTargetText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		allTargetText.addModifyListener(e -> updateLaunchConfigurationDialog());

		cleanTargetLabel = new Label(cmakeGroup, SWT.NONE);
		cleanTargetLabel.setText(Messages.CMakeBuildTab_CleanTarget);
		cleanTargetLabel.setToolTipText(Messages.CMakeBuildTab_cleanTargetTooltip);

		cleanTargetText = new Text(cmakeGroup, SWT.BORDER);
		cleanTargetText.setToolTipText(Messages.CMakeBuildTab_cleanTargetTooltip);
		cleanTargetText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		cleanTargetText.addModifyListener(e -> updateLaunchConfigurationDialog());

		Composite buildTypeComp = new Composite(cmakeGroup, SWT.NONE);
		buildTypeComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		buildTypeComp.setLayout(new GridLayout(3, false));

		buildTypeLabel = new Label(buildTypeComp, SWT.NONE);
		buildTypeLabel.setText(Messages.CMakeBuildTab_BuildType);
		buildTypeLabel.setToolTipText(Messages.CMakeBuildTab_BuildType_Tooltip);

		buildTypeCombo = new Combo(buildTypeComp, SWT.DROP_DOWN);
		buildTypeCombo.setItems(buildTypes);
		buildTypeCombo.setToolTipText(Messages.CMakeBuildTab_BuildTypeCombo_Tooltip);
		buildTypeCombo.addModifyListener(e -> updateLaunchConfigurationDialog());
		buildTypeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		usedForLaunchModeLabel = new Label(buildTypeComp, SWT.NONE);
	}

	/**
	 * When use CMake defaults is selected, restore the defaults
	 */
	private void showDefaultsInUi() {
		if (useDefaultCmakeSettings.getSelection()) {
			restoreProperties(getBuildConfiguration().getDefaultProperties());
		}
	}

	/**
	 * Updates the enabled state of the CMake settings controls based on useUiCmakeSettings checkbox
	 */
	private void updateEnablement() {
		boolean isDefaultCMakeProperties = useDefaultCmakeSettings.getSelection();
		boolean enabled = !isDefaultCMakeProperties;
		generatorLabel.setEnabled(enabled);
		generatorCombo.setEnabled(enabled);
		cmakeArgsLabel.setEnabled(enabled);
		cmakeArgsText.setEnabled(enabled);
		buildCommandLabel.setEnabled(enabled);
		buildCommandText.setEnabled(enabled);
		allTargetLabel.setEnabled(enabled);
		allTargetText.setEnabled(enabled);
		cleanTargetLabel.setEnabled(enabled);
		cleanTargetText.setEnabled(enabled);
		buildTypeLabel.setEnabled(enabled);
		buildTypeCombo.setEnabled(enabled);
		usedForLaunchModeLabel.setEnabled(enabled);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// Set defaults for Build Settings
		ICBuildConfiguration buildConfig = getBuildConfiguration();
		buildConfig.setProperty(CMakeBuildConfiguration.CMAKE_USE_DEFAULT_CMAKE_SETTINGS,
				CMakeBuildConfiguration.CMAKE_USE_DEFAULT_CMAKE_SETTINGS_DEFAULT);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);

		ICBuildConfiguration buildConfig = getBuildConfiguration();

		boolean isDefaultCMakeProperties = Boolean
				.valueOf(buildConfig.getProperty(CMakeBuildConfiguration.CMAKE_USE_DEFAULT_CMAKE_SETTINGS));
		useDefaultCmakeSettings.setSelection(isDefaultCMakeProperties);

		if (isDefaultCMakeProperties) {
			restoreProperties(buildConfig.getDefaultProperties());
		} else {
			restoreProperties(buildConfig.getProperties());
		}

		updateEnablement();
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);

		ICBuildConfiguration buildConfig = getBuildConfiguration();

		String generator = generatorCombo.getText().trim();
		buildConfig.setProperty(CMakeBuildConfiguration.CMAKE_GENERATOR, generator);

		String cmakeArgs = cmakeArgsText.getText().trim();
		if (!cmakeArgs.isEmpty()) {
			buildConfig.setProperty(CMakeBuildConfiguration.CMAKE_ARGUMENTS, cmakeArgs);
		} else {
			buildConfig.removeProperty(CMakeBuildConfiguration.CMAKE_ARGUMENTS);
		}

		String buildCommand = buildCommandText.getText().trim();
		if (!buildCommand.isEmpty()) {
			buildConfig.setProperty(CMakeBuildConfiguration.CMAKE_BUILD_COMMAND, buildCommand);
		} else {
			buildConfig.removeProperty(CMakeBuildConfiguration.CMAKE_BUILD_COMMAND);
		}

		String allTarget = allTargetText.getText().trim();
		if (!allTarget.isEmpty()) {
			buildConfig.setProperty(CMakeBuildConfiguration.CMAKE_ALL_TARGET, allTarget);
		} else {
			buildConfig.removeProperty(CMakeBuildConfiguration.CMAKE_ALL_TARGET);
		}

		String cleanTarget = cleanTargetText.getText().trim();
		if (!cleanTarget.isEmpty()) {
			buildConfig.setProperty(CMakeBuildConfiguration.CMAKE_CLEAN_TARGET, cleanTarget);
		} else {
			buildConfig.removeProperty(CMakeBuildConfiguration.CMAKE_CLEAN_TARGET);
		}

		boolean isDefaultCMakeProperties = useDefaultCmakeSettings.getSelection();
		buildConfig.setProperty(CMakeBuildConfiguration.CMAKE_USE_DEFAULT_CMAKE_SETTINGS,
				Boolean.toString(isDefaultCMakeProperties));

		String buildType = buildTypeCombo.getText().trim();
		if (!(buildType.isBlank() || isDefaultCMakeProperties)) {
			buildConfig.setProperty(CMakeBuildConfiguration.CMAKE_BUILD_TYPE, buildType);
		} else {
			buildConfig.removeProperty(CMakeBuildConfiguration.CMAKE_BUILD_TYPE);
		}
	}

	@Override
	protected void saveProperties(Map<String, String> properties) {
		super.saveProperties(properties);
		properties.put(CMakeBuildConfiguration.CMAKE_GENERATOR, generatorCombo.getText().trim());
		properties.put(CMakeBuildConfiguration.CMAKE_ARGUMENTS, cmakeArgsText.getText().trim());
		properties.put(CMakeBuildConfiguration.CMAKE_BUILD_COMMAND, buildCommandText.getText().trim());
		properties.put(CMakeBuildConfiguration.CMAKE_ALL_TARGET, allTargetText.getText().trim());
		properties.put(CMakeBuildConfiguration.CMAKE_CLEAN_TARGET, cleanTargetText.getText().trim());
		properties.put(CMakeBuildConfiguration.CMAKE_BUILD_TYPE, buildTypeCombo.getText().trim());
	}

	@Override
	protected void restoreProperties(Map<String, String> properties) {
		super.restoreProperties(properties);

		String gen = properties.getOrDefault(CMakeBuildConfiguration.CMAKE_GENERATOR,
				CMakeBuildConfiguration.CMAKE_GENERATOR_DEFAULT);
		generatorCombo.setText(gen);

		String cmakeArgs = properties.getOrDefault(CMakeBuildConfiguration.CMAKE_ARGUMENTS,
				CMakeBuildConfiguration.CMAKE_ARGUMENTS_DEFAULT);
		cmakeArgsText.setText(cmakeArgs);

		String buildCmd = properties.getOrDefault(CMakeBuildConfiguration.CMAKE_BUILD_COMMAND,
				CMakeBuildConfiguration.CMAKE_BUILD_COMMAND_DEFAULT);
		buildCommandText.setText(buildCmd);

		String allTarget = properties.getOrDefault(CMakeBuildConfiguration.CMAKE_ALL_TARGET,
				CMakeBuildConfiguration.CMAKE_ALL_TARGET_DEFAULT);
		allTargetText.setText(allTarget);

		String cleanTarget = properties.getOrDefault(CMakeBuildConfiguration.CMAKE_CLEAN_TARGET,
				CMakeBuildConfiguration.CMAKE_CLEAN_TARGET_DEFAULT);
		cleanTargetText.setText(cleanTarget);

		// Default build type: Debug for Debug launch mode, Release for Run and other launch modes.
		String defaultBuildType = getDefaultBuildType();
		String buildType = properties.getOrDefault(CMakeBuildConfiguration.CMAKE_BUILD_TYPE, defaultBuildType);
		buildTypeCombo.setText(buildType);

		ILaunchMode launchMode = DebugPlugin.getDefault().getLaunchManager()
				.getLaunchMode(getBuildConfiguration().getLaunchMode());
		String launchModeLabel = launchMode != null ? launchMode.getLabel() : getBuildConfiguration().getLaunchMode();
		usedForLaunchModeLabel.setText(MessageFormat.format(Messages.CMakeBuildTab_UsedForLaunchMode, launchModeLabel));
	}

	@Override
	public String getName() {
		return Messages.CMakeBuildTab_Cmake;
	}

	private String getDefaultBuildType() {
		return ILaunchManager.DEBUG_MODE.equals(getBuildConfiguration().getLaunchMode()) ? buildTypes[0]
				: buildTypes[1];
	}

}
