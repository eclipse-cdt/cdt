/**********************************************************************
 * Copyright (c) 2002 - 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.launch.internal.ui;

import java.util.Map;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.launch.ui.CLaunchConfigurationTab;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public abstract class AbstractCDebuggerTab extends CLaunchConfigurationTab {

	protected ILaunchConfiguration fLaunchConfiguration;
	protected ILaunchConfigurationWorkingCopy fWorkingCopy;
	protected ICDebugConfiguration fCurrentDebugConfig;

	// Dynamic Debugger UI widgets
	protected ILaunchConfigurationTab fDynamicTab;
	protected Composite fDynamicTabHolder;
	private boolean fInitDefaults;

	protected void setDebugConfig(ICDebugConfiguration config) {
		fCurrentDebugConfig = config;
	}

	protected ICDebugConfiguration getDebugConfig() {
		return fCurrentDebugConfig;
	}

	protected ILaunchConfigurationTab getDynamicTab() {
		return fDynamicTab;
	}

	protected void setDynamicTab(ILaunchConfigurationTab tab) {
		fDynamicTab = tab;
	}

	protected Composite getDynamicTabHolder() {
		return fDynamicTabHolder;
	}

	protected void setDynamicTabHolder(Composite tabHolder) {
		fDynamicTabHolder = tabHolder;
	}

	protected ILaunchConfigurationWorkingCopy getLaunchConfigurationWorkingCopy() {
		return fWorkingCopy;
	}

	protected void setLaunchConfiguration(ILaunchConfiguration launchConfiguration) {
		fLaunchConfiguration = launchConfiguration;
		setLaunchConfigurationWorkingCopy( null );
	}

	protected ILaunchConfiguration getLaunchConfiguration() {
		return fLaunchConfiguration;
	}

	protected void setLaunchConfigurationWorkingCopy(ILaunchConfigurationWorkingCopy workingCopy) {
		fWorkingCopy = workingCopy;
	}

	/**
	 * Overridden here so that any error message in the dynamic UI gets returned.
	 * 
	 * @see ILaunchConfigurationTab#getErrorMessage()
	 */
	public String getErrorMessage() {
		ILaunchConfigurationTab tab = getDynamicTab();
		if ((super.getErrorMessage() != null) || (tab == null)) {
			return super.getErrorMessage();
		}
		return tab.getErrorMessage();
	}

	/**
	 * Notification that the user changed the selection of the Debugger.
	 */
	protected void handleDebuggerChanged() {
		loadDynamicDebugArea();

		// always set the newly created area with defaults
		ILaunchConfigurationWorkingCopy wc = getLaunchConfigurationWorkingCopy();
		if (getDynamicTab() == null) {
			// remove any debug specfic args from the config
			if (wc == null) {
				if (getLaunchConfiguration().isWorkingCopy()) {
					wc = (ILaunchConfigurationWorkingCopy) getLaunchConfiguration();
				}
			}
			if (wc != null) {
				wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_SPECIFIC_ATTRS_MAP, (Map) null);
			}
		} else {
			if (wc == null) {
				try {
					if (getLaunchConfiguration().isWorkingCopy()) {
						setLaunchConfigurationWorkingCopy((ILaunchConfigurationWorkingCopy)getLaunchConfiguration());
					} else {
						setLaunchConfigurationWorkingCopy(getLaunchConfiguration().getWorkingCopy());
					}
					wc = getLaunchConfigurationWorkingCopy();

				} catch (CoreException e) {
					return;
				}
			}
			if (initDefaults()) {
				getDynamicTab().setDefaults(wc);
			}
			setInitializeDefault(false);
			getDynamicTab().initializeFrom(wc);
		}
		updateLaunchConfigurationDialog();
	}

	/**
	 * Show the contributed piece of UI that was registered for the debugger id
	 * of the currently selected debugger.
	 */
	protected void loadDynamicDebugArea() {
		// Dispose of any current child widgets in the tab holder area
		Control[] children = getDynamicTabHolder().getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].dispose();
		}

		// Retrieve the dynamic UI for the current Debugger
		ICDebugConfiguration debugConfig = getConfigForCurrentDebugger();
		if (debugConfig == null) {
			setDynamicTab(null);
		} else {
			ILaunchConfigurationTab tab = null;
			try {
				tab = CDebugUIPlugin.getDefault().getDebuggerPage(debugConfig.getID());
			} catch (CoreException e) {
				LaunchUIPlugin.errorDialog(LaunchUIPlugin.getResourceString("AbstractCDebuggerTab.ErrorLoadingDebuggerPage"), e.getStatus());  //$NON-NLS-1$
			}
			setDynamicTab(tab);
		}
		setDebugConfig(debugConfig);
		if (getDynamicTab() == null) {
			return;
		}
		// Ask the dynamic UI to create its Control
		getDynamicTab().setLaunchConfigurationDialog(getLaunchConfigurationDialog());
		getDynamicTab().createControl(getDynamicTabHolder());
		getDynamicTab().getControl().setVisible(true);
		getDynamicTabHolder().layout(true);
	}

	abstract protected ICDebugConfiguration getConfigForCurrentDebugger();
	abstract public void createControl(Composite parent);

	
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		ILaunchConfigurationTab dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			dynamicTab.activated(workingCopy);
		}
	}
	
	public void initializeFrom(ILaunchConfiguration config) {
		setLaunchConfiguration(config);
		ILaunchConfigurationTab dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			dynamicTab.initializeFrom(config);
		}
	}

	public void performApply(ILaunchConfigurationWorkingCopy config) {
		if (getDebugConfig() != null) {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, getDebugConfig().getID());
			ILaunchConfigurationTab dynamicTab = getDynamicTab();
			if (dynamicTab == null) {
				config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_SPECIFIC_ATTRS_MAP, (Map) null);
			} else {
				dynamicTab.performApply(config);
			}
		}
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		setLaunchConfigurationWorkingCopy(config);
		ILaunchConfigurationTab dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			dynamicTab.setDefaults(config);
			setInitializeDefault(false);
		}
	}

	public boolean isValid(ILaunchConfiguration config) {
		setErrorMessage(null);
		setMessage(null);
		if (getDebugConfig() == null) {
			setErrorMessage(LaunchUIPlugin.getResourceString("AbstractCDebuggerTab.No_debugger_available")); //$NON-NLS-1$
			return false;
		}

		ILaunchConfigurationTab dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			return dynamicTab.isValid(config);
		}
		return true;
	}

	protected void setInitializeDefault(boolean init) {
		fInitDefaults = init;
	}

	protected boolean initDefaults() {
		return fInitDefaults;
	}

	public Image getImage() {
		return LaunchImages.get(LaunchImages.IMG_VIEW_DEBUGGER_TAB);
	}

	public String getName() {
		return LaunchUIPlugin.getResourceString("AbstractCDebuggerTab.Debugger"); //$NON-NLS-1$
	}

}
