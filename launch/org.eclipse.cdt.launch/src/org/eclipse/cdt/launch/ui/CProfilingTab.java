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
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @since 7.2
 */
public class CProfilingTab extends CLaunchConfigurationTab {

	private ILaunchConfigurationTab tab;
	
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
					if (obj instanceof ILaunchConfigurationTab) {
						tab = (ILaunchConfigurationTab)obj;
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
		if (tab != null)
			tab.createControl(parent);
	}

	@Override
	public Control getControl() {
		if (tab != null)
			return tab.getControl();
		return null;
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		if (tab != null)
			tab.setDefaults(configuration);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		if (tab != null)
			tab.initializeFrom(configuration);
	}

	@Override
	public void dispose() {
		if (tab != null)
			tab.dispose();
		super.dispose();
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (tab != null)
			tab.performApply(configuration);
	}

	@Override
	public String getErrorMessage() {
		if (tab != null)
			return tab.getErrorMessage();
		return null;
	}

	@Override
	public String getMessage() {
		if (tab != null)
			return tab.getMessage();
		return null;
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		if (tab != null)
			return tab.isValid(launchConfig);
		return false;
	}

	@Override
	public boolean canSave() {
		if (tab != null)
			return tab.canSave();
		return false;
	}

	@Override
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		if (tab != null)
			tab.setLaunchConfigurationDialog(dialog);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void launched(ILaunch launch) {
		if (tab != null)
			tab.launched(launch);
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
		return LaunchImages.get(LaunchImages.IMG_VIEW_TIME_TAB);
	}

	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		if (tab != null)
			tab.activated(workingCopy);
	}

	@Override
	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {
		if (tab != null)
			tab.deactivated(workingCopy);
	}

}
