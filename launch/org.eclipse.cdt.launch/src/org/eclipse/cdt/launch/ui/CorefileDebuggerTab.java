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
package org.eclipse.cdt.launch.ui;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.launch.internal.ui.AbstractCDebuggerTab;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.help.WorkbenchHelp;

public class CorefileDebuggerTab extends AbstractCDebuggerTab {
	protected Combo fDCombo;

	private boolean initializingComboBox = false;

	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		WorkbenchHelp.setHelp(getControl(), ICDTLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_DEBBUGER_TAB);
				
		GridLayout topLayout = new GridLayout(2, false);
		comp.setLayout(topLayout);
		Label dlabel = new Label(comp, SWT.NONE);
		dlabel.setText(LaunchUIPlugin.getResourceString("Launch.common.DebuggerColon")); //$NON-NLS-1$
		fDCombo = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
		fDCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleDebuggerChanged();
			}
		});
		Group debuggerGroup = new Group(comp, SWT.SHADOW_ETCHED_IN);
		debuggerGroup.setText(LaunchUIPlugin.getResourceString("CorefileDebuggerTab.Debugger_Options")); //$NON-NLS-1$
		setDynamicTabHolder(debuggerGroup);
		GridLayout tabHolderLayout = new GridLayout();
		tabHolderLayout.marginHeight = 0;
		tabHolderLayout.marginWidth = 0;
		tabHolderLayout.numColumns = 1;
		getDynamicTabHolder().setLayout(tabHolderLayout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		getDynamicTabHolder().setLayoutData(gd);
	}

	protected void loadDebuggerComboBox(ILaunchConfiguration config, String selection) {
		if (initializingComboBox) {
			return;
		}
		initializingComboBox = true;
		ICDebugConfiguration[] debugConfigs;
		String configPlatform = getPlatform(config);
		ICElement ce = getContext(config, null);
		String projectPlatform = "*"; //$NON-NLS-1$
		String projectCPU = "*"; //$NON-NLS-1$
		if (ce != null) {
			try {
				ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(ce.getCProject().getProject(), false);
				if (descriptor != null) {
					projectPlatform = descriptor.getPlatform();
				}
				IBinary bin = (IBinary) ce;
				projectCPU = bin.getCPU();
			} catch (Exception e) {
			}
		}
		fDCombo.removeAll();
		debugConfigs = CDebugCorePlugin.getDefault().getDebugConfigurations();
		int x = 0;
		int selndx = -1;
		for (int i = 0; i < debugConfigs.length; i++) {
			if (debugConfigs[i].supportsMode(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE)) {
				String debuggerPlatform = debugConfigs[i].getPlatform();
				boolean platformMatch = configPlatform.equals(projectPlatform);
				if (debuggerPlatform.equalsIgnoreCase(projectPlatform) || (platformMatch && projectPlatform.equals("*"))) { //$NON-NLS-1$
					if (debugConfigs[i].supportsCPU(projectCPU)) {
						fDCombo.add(debugConfigs[i].getName());
						fDCombo.setData(Integer.toString(x), debugConfigs[i]);
						// select first exact matching debugger for platform or requested selection
						if ((selndx == -1 && debuggerPlatform.equalsIgnoreCase(projectPlatform))
							|| selection.equals(debugConfigs[i].getID())) {
							selndx = x;
						}
						x++;
					}
				}
			}
		}
		// if no selection meaning nothing in config the force initdefault on tab
		setInitializeDefault(selection.equals("") ? true : false); //$NON-NLS-1$

		fDCombo.select(selndx == -1 ? 0 : selndx);
		//The behaviour is undefined for if the callbacks should be triggered for this,
		//so to avoid unnecessary confusion, we force an update.
		handleDebuggerChanged();
		getControl().getParent().layout(true);
		initializingComboBox = false;
	}

	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		super.activated(workingCopy);
		try {
			String id = workingCopy.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, ""); //$NON-NLS-1$
			if (getDebugConfig() == null || !getDebugConfig().getID().equals(id) || !validateDebuggerConfig(workingCopy)) {
				loadDebuggerComboBox(workingCopy, id);
			}
		} catch (CoreException e) {
		}
	}
	
	public void initializeFrom(ILaunchConfiguration config) {
		super.initializeFrom(config);
		try {
			String id = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, ""); //$NON-NLS-1$
			loadDebuggerComboBox(config, id);
		} catch (CoreException e) {
			return;
		}
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		super.setDefaults(config);
		config.setAttribute(
			ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
			ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE);
	}

	public boolean isValid(ILaunchConfiguration config) {
		if (!validateDebuggerConfig(config)) {
			setErrorMessage(LaunchUIPlugin.getResourceString("CorefileDebuggerTab.No_debugger_available")); //$NON-NLS-1$
			return false;
		}
		return super.isValid(config);
	}

	private boolean validateDebuggerConfig(ILaunchConfiguration config) {
		String platform = getPlatform(config);
		ICElement ce = getContext(config, null);
		String projectPlatform = "*"; //$NON-NLS-1$
		String projectCPU = "*"; //$NON-NLS-1$
		if (ce != null) {
			try {
				ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(ce.getCProject().getProject(), false);
				if (descriptor != null) {
					projectPlatform = descriptor.getPlatform();
				}
				IBinary bin = (IBinary) ce;
				projectCPU = bin.getCPU();
			} catch (Exception e) {
			}
		}
		ICDebugConfiguration debugConfig = getDebugConfig();
		if (debugConfig == null) {
			return false;
		}
		String debuggerPlatform = debugConfig.getPlatform();
		boolean platformMatch = platform.equals(projectPlatform);
		if (debuggerPlatform.equalsIgnoreCase(projectPlatform) || (platformMatch && projectPlatform.equals("*"))) { //$NON-NLS-1$
			if (debugConfig.supportsCPU(projectCPU)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return the class that implements <code>ILaunchConfigurationTab</code>
	 * that is registered against the debugger id of the currently selected debugger.
	 */
	protected ICDebugConfiguration getConfigForCurrentDebugger() {
		int selectedIndex = fDCombo.getSelectionIndex();
		return (ICDebugConfiguration) fDCombo.getData(Integer.toString(selectedIndex));
	}

	/**
	 * @see org.eclipse.cdt.launch.internal.ui.AbstractCDebuggerTab#handleDebuggerChanged()
	 */
	protected void handleDebuggerChanged() {
		super.handleDebuggerChanged();
	}

}
