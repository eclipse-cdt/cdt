/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.launch.internal.ui;

import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.launch.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.ui.CLaunchConfigurationTab;
import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public abstract class AbstractCDebuggerTab extends CLaunchConfigurationTab {

	protected ILaunchConfiguration fLaunchConfiguration;
	protected ILaunchConfigurationWorkingCopy fWorkingCopy;
	protected ICDebugConfiguration fCurrentDebugConfig;

	// Dynamic Debugger UI widgets
	protected ILaunchConfigurationTab fDynamicTab;
	protected Composite fDynamicTabHolder;

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
		else {
			return tab.getErrorMessage();
		}
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
		}
		else {
			if (wc == null) {
				try {
					if (getLaunchConfiguration().isWorkingCopy()) {
						// get a fresh copy to work on
						wc = ((ILaunchConfigurationWorkingCopy) getLaunchConfiguration()).getOriginal().getWorkingCopy();
					}
					else {
						wc = getLaunchConfiguration().getWorkingCopy();
					}
				}
				catch (CoreException e) {
					return;
				}
			}
			getDynamicTab().setDefaults(wc);
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
		}
		else {
			setDynamicTab(CDebugUIPlugin.getDefault().getDebuggerPage(debugConfig.getID()));
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
	abstract public String getName();

	public void initializeFrom(ILaunchConfiguration config) {
		setLaunchConfiguration(config);
		ILaunchConfigurationTab dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			dynamicTab.initializeFrom(config);
		}
	}

	public void performApply(ILaunchConfigurationWorkingCopy config) {
		if ( getDebugConfig() != null ) {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, getDebugConfig().getID());
			ILaunchConfigurationTab dynamicTab = getDynamicTab();
			if (dynamicTab == null) {
				config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_SPECIFIC_ATTRS_MAP, (Map) null);
			}
			else {
				dynamicTab.performApply(config);
			}
		}
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		setLaunchConfigurationWorkingCopy(config);
		ILaunchConfigurationTab dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			dynamicTab.setDefaults(config);
		}
	}

	public boolean isValid(ILaunchConfiguration config) {
		setErrorMessage(null);
		setMessage(null);
		if (getDebugConfig() == null) {
			setErrorMessage("No debugger avalible");
			return false;
		}

		ILaunchConfigurationTab dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			return dynamicTab.isValid(config);
		}
		return true;
	}

}
