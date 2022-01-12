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
package org.eclipse.cdt.launch.ui.corebuild;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.ICBuildConfigurationProvider;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.debug.core.launch.CoreBuildLaunchConfigDelegate;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.ui.ILaunchBarLaunchConfigDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 * Common utilities for Core Build launch configuration tabs.
 *
 * @since 9.1
 */
public abstract class CommonBuildTab extends AbstractLaunchConfigurationTab {

	private Combo tcCombo;
	private ICBuildConfiguration buildConfig;
	private IToolChain[] toolchains;

	private IToolChain currentToolchain;
	private IProject project;

	private Map<ICBuildConfiguration, Map<String, String>> savedProperties = new HashMap<>();

	private static IToolChainManager tcManager = LaunchUIPlugin.getService(IToolChainManager.class);
	private static ICBuildConfigurationManager bcManager = LaunchUIPlugin.getService(ICBuildConfigurationManager.class);

	/**
	 * @since 9.2
	 */
	protected String getBuildConfigProviderId() {
		return null;
	}

	/**
	 * @since 9.2
	 */
	protected void saveProperties(Map<String, String> properties) {
	}

	/**
	 * @since 9.2
	 */
	protected void restoreProperties(Map<String, String> properties) {
	}

	/**
	 * @since 9.2
	 */
	protected Control createToolchainSelector(Composite parent) {
		Group tcGroup = new Group(parent, SWT.NONE);
		tcGroup.setText(LaunchMessages.CommonBuildTab_Toolchain);
		tcGroup.setLayout(new GridLayout());

		tcCombo = new Combo(tcGroup, SWT.READ_ONLY);
		tcCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		tcCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (buildConfig != null) {
					Map<String, String> saved = new HashMap<>();
					saveProperties(saved);
					savedProperties.put(buildConfig, saved);
				}

				if (toolchainChanged()) {
					Map<String, String> saved = savedProperties.get(buildConfig);
					if (saved != null) {
						restoreProperties(saved);
					}
				}
			}
		});

		return tcGroup;
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		if (tcCombo == null) {
			return;
		}

		try {
			project = CoreBuildLaunchConfigDelegate.getProject(configuration);

			ICBuildConfigurationProvider bcProvider = bcManager.getProvider(getBuildConfigProviderId());
			ILaunchTarget target = getLaunchTarget();
			toolchains = bcProvider.getSupportedToolchains(tcManager.getToolChainsMatching(target.getAttributes()))
					.toArray(new IToolChain[0]);

			tcCombo.removeAll();
			if (toolchains.length > 0) {
				tcCombo.add(String.format(LaunchMessages.CommonBuildTab_Default, toolchains[0].getName()));
			} else {
				tcCombo.add(LaunchMessages.CommonBuildTab_NotFound);
			}

			for (IToolChain tc : toolchains) {
				tcCombo.add(tc.getName());
			}

			tcCombo.select(0);

			String toolchainId = configuration.getAttribute(ICBuildConfiguration.TOOLCHAIN_ID, (String) null);
			if (toolchainId != null) {
				String typeId = configuration.getAttribute(ICBuildConfiguration.TOOLCHAIN_TYPE, ""); //$NON-NLS-1$
				IToolChain toolchain = tcManager.getToolChain(typeId, toolchainId);
				if (toolchain != null) {
					for (int i = 0; i < toolchains.length; i++) {
						if (toolchains[i] == toolchain) {
							tcCombo.select(i + 1);
							break;
						}
					}
				}
			}

			toolchainChanged();
		} catch (CoreException e) {
			LaunchUIPlugin.log(e.getStatus());
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (tcCombo == null) {
			return;
		}

		int i = tcCombo.getSelectionIndex();
		if (i == 0) {
			configuration.removeAttribute(ICBuildConfiguration.TOOLCHAIN_ID);
			configuration.removeAttribute(ICBuildConfiguration.TOOLCHAIN_TYPE);
		} else {
			IToolChain tc = toolchains[i - 1];
			configuration.setAttribute(ICBuildConfiguration.TOOLCHAIN_ID, tc.getId());
			configuration.setAttribute(ICBuildConfiguration.TOOLCHAIN_TYPE, tc.getTypeId());
		}
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.removeAttribute(ICBuildConfiguration.TOOLCHAIN_ID);
		configuration.removeAttribute(ICBuildConfiguration.TOOLCHAIN_TYPE);
	}

	private boolean toolchainChanged() {
		int i = tcCombo.getSelectionIndex();
		if (i < 0 || toolchains.length == 0) {
			buildConfig = null;
			return false;
		} else if (i == 0) {
			i = 1;
		}

		IToolChain newToolchain = toolchains[i - 1];
		if (newToolchain == currentToolchain) {
			return false;
		}
		currentToolchain = newToolchain;

		String mode = getLaunchConfigurationDialog().getMode();
		try {
			buildConfig = bcManager.getBuildConfiguration(project, newToolchain, mode, new NullProgressMonitor());
		} catch (CoreException e) {
			LaunchUIPlugin.log(e.getStatus());
		}

		return true;
	}

	public ILaunchBarLaunchConfigDialog getLaunchBarLaunchConfigDialog() {
		ILaunchConfigurationDialog dialog = getLaunchConfigurationDialog();
		return dialog instanceof ILaunchBarLaunchConfigDialog ? (ILaunchBarLaunchConfigDialog) dialog : null;
	}

	public ILaunchTarget getLaunchTarget() {
		ILaunchBarLaunchConfigDialog dialog = getLaunchBarLaunchConfigDialog();
		return dialog != null ? dialog.getLaunchTarget() : null;
	}

	/**
	 * @deprecated Just use getBuildConfiguration()
	 */
	@Deprecated
	public ICBuildConfiguration getBuildConfiguration(ILaunchConfiguration configuration) throws CoreException {
		return buildConfig;
	}

	/**
	 * @since 9.2
	 */
	public ICBuildConfiguration getBuildConfiguration() {
		return buildConfig;
	}

}
