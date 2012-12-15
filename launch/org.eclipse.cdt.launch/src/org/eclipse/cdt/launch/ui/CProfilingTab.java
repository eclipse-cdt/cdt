/*******************************************************************************
 * Copyright (c) 2012 Red Hat Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.launch.ui;

import org.eclipse.cdt.launch.internal.ui.LaunchImages;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * @since 7.2
 */
public class CProfilingTab extends AbstractLaunchConfigurationTab {

	private AbstractLaunchConfigurationTab tabDelegate;
    public static final String TAB_ID = "org.eclipse.cdt.cdi.launch.applicationLaunch.profilingTab"; //$NON-NLS-1$
	
	public CProfilingTab() {
		super();
		IExtensionPoint extPoint = Platform.getExtensionRegistry()
				.getExtensionPoint(LaunchUIPlugin.PLUGIN_ID,
						"profilingProvider"); //$NON-NLS-1$
		IConfigurationElement[] configs = extPoint.getConfigurationElements();
		for (IConfigurationElement config : configs) {
			if (config.getName().equals("provider")) { //$NON-NLS-1$
				try {
					Object obj = config
							.createExecutableExtension("tab"); //$NON-NLS-1$
					if (obj instanceof AbstractLaunchConfigurationTab) {
						tabDelegate = (AbstractLaunchConfigurationTab)obj;
						break;
					}
				} catch (CoreException e) {
					// continue, perhaps another configuration will succeed
				}
			}
		}

	}
	
	@Override
	public void createControl(Composite parent) {
		if (tabDelegate != null)
			tabDelegate.createControl(parent);
		else {
			Composite profilingComp = new Composite(parent, SWT.NONE);
			setControl(profilingComp);
			GridLayout profilingLayout = new GridLayout();
			profilingLayout.numColumns = 1;
			profilingLayout.marginHeight = 5;
			profilingLayout.marginWidth = 5;
			profilingComp.setLayout(profilingLayout);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			profilingComp.setLayoutData(gd);
			Label label = new Label(profilingComp, SWT.WRAP);
			label.setText(LaunchMessages.CProfilingTab_No_Profiling_Providers);
			label.setEnabled(true);
		}
	}

	@Override
	public Control getControl() {
		if (tabDelegate != null)
			return tabDelegate.getControl();
		return super.getControl();
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		if (tabDelegate != null)
			tabDelegate.setDefaults(configuration);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		if (tabDelegate != null)
			tabDelegate.initializeFrom(configuration);
	}

	@Override
	public void dispose() {
		if (tabDelegate != null)
			tabDelegate.dispose();
		super.dispose();
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (tabDelegate != null)
			tabDelegate.performApply(configuration);
	}

	@Override
	public String getErrorMessage() {
		if (tabDelegate != null)
			return tabDelegate.getErrorMessage();
		return LaunchMessages.CProfilingTab_No_Profiling_Providers;
	}

	@Override
	public String getMessage() {
		if (tabDelegate != null)
			return tabDelegate.getMessage();
		return null;
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		if (tabDelegate != null)
			return tabDelegate.isValid(launchConfig);
		return false;
	}

	@Override
	public boolean canSave() {
		if (tabDelegate != null)
			return tabDelegate.canSave();
		return false;
	}

	@Override
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		if (tabDelegate != null)
			tabDelegate.setLaunchConfigurationDialog(dialog);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void launched(ILaunch launch) {
		if (tabDelegate != null)
			tabDelegate.launched(launch);
	}

	@Override
	public String getName() {
		return LaunchMessages.CProfilingTab_Profiling;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	@Override
	public Image getImage() {
		return LaunchImages.get(LaunchImages.IMG_VIEW_PROFILER_TAB);
	}

	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		if (tabDelegate != null)
			tabDelegate.activated(workingCopy);
	}

	@Override
	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {
		if (tabDelegate != null)
			tabDelegate.deactivated(workingCopy);
	}

	@Override
	public String getId() {
		return TAB_ID;
	}
	
	// All other AbstractLaunchConfigurationTab methods are intentionally not overridden
	// and use the super's methods.
	
}
