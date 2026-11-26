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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class MakeBuildSettingsTab extends CommonBuildTab {
	private static final String TRUE = String.valueOf(true);
	private static final String FALSE = String.valueOf(false);
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private static final String defaultCdbFileName = "compile_commands.json"; //$NON-NLS-1$

	private Button projectButton;
	private Button configButton;
	private Button customButton;
	private Text customText;
	private Button useTopLevelMakefileButton;
	private Text buildCmdText;
	private Text cleanCmdText;

	private Button cdbNoneButton;
	private Button cdbLocationBuildDirectoryButton;
	private Button cdbLocationCustomFileButton;
	private Text cdbLocationCustomFileText;

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

		customButton = new Button(outputGroup, SWT.RADIO);
		customButton.setText(Messages.MakeBuildSettingsTab_BuildInCustomDir);
		customButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		customText = new Text(outputGroup, SWT.BORDER);
		customText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Group cmdGroup = new Group(comp, SWT.NONE);
		cmdGroup.setText(Messages.MakeBuildSettingsTab_BuildCommands);
		cmdGroup.setLayout(new GridLayout(2, false));
		cmdGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		useTopLevelMakefileButton = new Button(cmdGroup, SWT.CHECK);
		useTopLevelMakefileButton.setText(Messages.MakeBuildSettingsTab_UseTopLevelMakefile);
		useTopLevelMakefileButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label label = new Label(cmdGroup, SWT.NONE);
		label.setText(Messages.MakeBuildSettingsTab_Build);

		buildCmdText = new Text(cmdGroup, SWT.BORDER);
		buildCmdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		label = new Label(cmdGroup, SWT.NONE);
		label.setText(Messages.MakeBuildSettingsTab_Clean);

		cleanCmdText = new Text(cmdGroup, SWT.BORDER);
		cleanCmdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Group cdbGroup = new Group(comp, SWT.NONE);
		cdbGroup.setText(Messages.MakeBuildSettingsTab_IndexerIncludePathsAndMacros);
		cdbGroup.setLayout(new GridLayout());
		cdbGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		cdbNoneButton = new Button(cdbGroup, SWT.RADIO);
		cdbNoneButton.setText(Messages.MakeBuildSettingsTab_IncludePathsAndMacrosExtractFromBuildStepConsoleOutput);
		cdbNoneButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		cdbLocationBuildDirectoryButton = new Button(cdbGroup, SWT.RADIO);
		cdbLocationBuildDirectoryButton
				.setText(Messages.MakeBuildSettingsTab_IncludePathsAndMacrosCDBFromBuildDirectory);
		cdbLocationBuildDirectoryButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		cdbLocationCustomFileButton = new Button(cdbGroup, SWT.RADIO);
		cdbLocationCustomFileButton.setText(Messages.MakeBuildSettingsTab_IncludePathsAndMacrosCDBCustomFile);
		cdbLocationCustomFileButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		cdbLocationCustomFileText = new Text(cdbGroup, SWT.BORDER);
		cdbLocationCustomFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		projectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (projectButton.getSelection()) {
					if (getBuildConfiguration() instanceof final StandardBuildConfiguration stdConfig) {
						final String buildDir = stdConfig.getProject().getFullPath().toString();
						customText.setText(buildDir);
					}
				}
			}
		});

		configButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (configButton.getSelection()) {
					if (getBuildConfiguration() instanceof final StandardBuildConfiguration stdConfig) {
						try {
							final String buildDir = stdConfig.getDefaultBuildContainer().getFullPath().toString();
							customText.setText(buildDir);
						} catch (CoreException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
		});

		customButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				customText.setEnabled(customButton.getSelection());
			}
		});

		customText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (cdbLocationBuildDirectoryButton.getSelection()) {
					final String cdbPath = new Path(customText.getText()).append(defaultCdbFileName).toString();
					cdbLocationCustomFileText.setText(cdbPath);
				}
			}
		});

		cdbLocationBuildDirectoryButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (cdbLocationBuildDirectoryButton.getSelection()) {
					final String cdbPath = new Path(customText.getText()).append(defaultCdbFileName).toString();
					cdbLocationCustomFileText.setText(cdbPath);
				}
			}
		});

		cdbLocationCustomFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cdbLocationCustomFileText.setEnabled(cdbLocationCustomFileButton.getSelection());
			}
		});
	}

	@Override
	public String getName() {
		return Messages.MakeBuildSettingsTab_Makefile;
	}

	private boolean isBuildContainerProjectRoot(ICBuildConfiguration buildConfiguration, IPath containerPath) {
		if (buildConfiguration instanceof final StandardBuildConfiguration stdConfig) {
			return stdConfig.getProject().getFullPath().addTrailingSeparator()
					.equals(containerPath.addTrailingSeparator());
		}
		return false;
	}

	private boolean isBuildContainerConfigurationDirectory(ICBuildConfiguration buildConfiguration,
			IPath containerPath) {
		if (buildConfiguration instanceof final StandardBuildConfiguration stdConfig) {
			try {
				return stdConfig.getDefaultBuildContainer().getFullPath().addTrailingSeparator()
						.equals(containerPath.addTrailingSeparator());
			} catch (CoreException e) {
				MakeUIPlugin.log(e.getStatus());
			}
		}
		return false;
	}

	private void updateControls(ICBuildConfiguration buildConfig, String container, String useTopLevelMakefile,
			String buildCommand, String cleanCommand, String scannerInfoCompilationDatabasePath) {
		final IPath containerLoc;
		if (container != null && !container.trim().isEmpty()) {
			containerLoc = new Path(container);
			if (isBuildContainerProjectRoot(buildConfig, containerLoc)) {
				projectButton.setSelection(true);
			} else if (isBuildContainerConfigurationDirectory(buildConfig, containerLoc)) {
				configButton.setSelection(true);
			} else {
				customButton.setSelection(true);
			}

			customText.setText(container);
			customText.setEnabled(customButton.getSelection());
		} else {
			containerLoc = null;
		}

		if (useTopLevelMakefile != null && !useTopLevelMakefile.trim().isEmpty()) {
			useTopLevelMakefileButton.setSelection(Boolean.valueOf(useTopLevelMakefile));
		}

		if (buildCommand != null && !buildCommand.trim().isEmpty()) {
			buildCmdText.setText(buildCommand);
		}

		if (cleanCommand != null && !cleanCommand.trim().isEmpty()) {
			cleanCmdText.setText(cleanCommand);
		}

		if (scannerInfoCompilationDatabasePath != null) {
			scannerInfoCompilationDatabasePath = scannerInfoCompilationDatabasePath.trim();
		}

		if (scannerInfoCompilationDatabasePath != null && !scannerInfoCompilationDatabasePath.isEmpty()) {
			cdbLocationCustomFileText.setText(scannerInfoCompilationDatabasePath);

			if (containerLoc != null
					&& new Path(scannerInfoCompilationDatabasePath).equals(containerLoc.append(defaultCdbFileName))) {
				cdbLocationBuildDirectoryButton.setSelection(true);
			} else {
				cdbLocationCustomFileButton.setSelection(true);
			}
		} else {
			cdbNoneButton.setSelection(true);
		}

		cdbLocationCustomFileText.setEnabled(cdbLocationCustomFileButton.getSelection());
	}

	@Override
	protected void restoreProperties(Map<String, String> properties) {
		// TODO Auto-generated method stub
		super.restoreProperties(properties);

		String container = properties.get(StandardBuildConfiguration.BUILD_CONTAINER);

		String useTopLevelMakefile = properties.get(StandardBuildConfiguration.BUILD_USE_TOP_LEVEL_MAKEFILE);

		String buildCommand = properties.get(StandardBuildConfiguration.BUILD_COMMAND);

		String cleanCommand = properties.get(StandardBuildConfiguration.CLEAN_COMMAND);

		String scannerInfoCompilationDatabasePath = properties
				.get(StandardBuildConfiguration.SCANNER_INFO_COMPILATION_DATABASE_PATH);

		updateControls(getBuildConfiguration(), container, useTopLevelMakefile, buildCommand, cleanCommand,
				scannerInfoCompilationDatabasePath);
	}

	@Override
	protected void saveProperties(Map<String, String> properties) {
		super.saveProperties(properties);

		try {
			ICBuildConfiguration buildConfig = getBuildConfiguration();
			if (buildConfig instanceof final StandardBuildConfiguration stdConfig) {
				String buildContainerString = null;
				if (configButton.getSelection()) {
					buildContainerString = stdConfig.getDefaultBuildContainer().getFullPath().toString();
				} else if (projectButton.getSelection()) {
					buildContainerString = stdConfig.getProject().getFullPath().toString();
				} else if (customButton.getSelection()) {
					buildContainerString = customText.getText().trim();
				}

				if (buildContainerString != null && !buildContainerString.isEmpty()) {
					properties.put(StandardBuildConfiguration.BUILD_CONTAINER, buildContainerString);
				}

				properties.put(StandardBuildConfiguration.BUILD_USE_TOP_LEVEL_MAKEFILE,
						useTopLevelMakefileButton.getSelection() ? TRUE : FALSE);

				String buildCommand = buildCmdText.getText().trim();
				if (!buildCommand.isEmpty()) {
					properties.put(StandardBuildConfiguration.BUILD_COMMAND, buildCommand);
				}

				String cleanCommand = cleanCmdText.getText().trim();
				if (!cleanCommand.isEmpty()) {
					properties.put(StandardBuildConfiguration.CLEAN_COMMAND, cleanCommand);
				}

				if (cdbNoneButton.getSelection()) {
					properties.remove(StandardBuildConfiguration.SCANNER_INFO_COMPILATION_DATABASE_PATH);
				} else if (cdbLocationBuildDirectoryButton.getSelection()) {
					properties.put(StandardBuildConfiguration.SCANNER_INFO_COMPILATION_DATABASE_PATH,
							new Path(buildContainerString).append(defaultCdbFileName).toString());
				} else {
					String cdbPath = cdbLocationCustomFileText.getText().trim();
					if (!cdbPath.isEmpty()) {
						properties.put(StandardBuildConfiguration.SCANNER_INFO_COMPILATION_DATABASE_PATH, cdbPath);
					}
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

		String useTopLevelMakefile = buildConfig.getProperty(StandardBuildConfiguration.BUILD_USE_TOP_LEVEL_MAKEFILE);

		String buildCommand = buildConfig.getProperty(StandardBuildConfiguration.BUILD_COMMAND);

		String cleanCommand = buildConfig.getProperty(StandardBuildConfiguration.CLEAN_COMMAND);

		String scannerInfoCompilationDatabasePath = buildConfig
				.getProperty(StandardBuildConfiguration.SCANNER_INFO_COMPILATION_DATABASE_PATH);

		updateControls(buildConfig, container, useTopLevelMakefile, buildCommand, cleanCommand,
				scannerInfoCompilationDatabasePath);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);

		try {
			ICBuildConfiguration buildConfig = getBuildConfiguration();
			if (buildConfig instanceof final StandardBuildConfiguration stdConfig) {
				if (configButton.getSelection()) {
					stdConfig.setBuildContainer(stdConfig.getDefaultBuildContainer());
				} else if (projectButton.getSelection()) {
					stdConfig.setBuildContainer(stdConfig.getProject());
				} else if (customButton.getSelection()) {
					String buildContainerString = customText.getText().trim();
					if (!buildContainerString.isEmpty()) {
						stdConfig.setBuildContainer(
								ResourcesPlugin.getWorkspace().getRoot().getFolder(new Path(buildContainerString)));
					}
				}

				stdConfig.setUseTopLevelMakefile(useTopLevelMakefileButton.getSelection());

				stdConfig.setBuildCommand(buildCmdText.getText());
				stdConfig.setCleanCommand(cleanCmdText.getText());

				final String cdbPath;
				if (cdbNoneButton.getSelection()) {
					cdbPath = EMPTY_STRING;
				} else if (cdbLocationBuildDirectoryButton.getSelection()) {
					cdbPath = stdConfig.getBuildContainer().getFullPath().append(defaultCdbFileName).toString();
				} else {
					cdbPath = cdbLocationCustomFileText.getText().trim();
				}

				stdConfig.setScannerInfoCompilationDatabasePath(cdbPath);
			}
		} catch (CoreException e) {
			MakeUIPlugin.log(e.getStatus());
		}
	}

}
