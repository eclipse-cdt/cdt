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

import java.util.Arrays;
import java.util.Comparator;

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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.help.WorkbenchHelp;

public class CDebuggerTab extends AbstractCDebuggerTab {

	protected Combo fDCombo;
	protected Button fStopInMain;
	protected Button fAttachButton;
	protected Button fRunButton;
	protected Button fVarBookKeeping;

	private final boolean DEFAULT_STOP_AT_MAIN = true;
	private boolean pageUpdated;

	public void createControl(Composite parent) {
		GridData gd;

		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		WorkbenchHelp.setHelp(getControl(), ICDTLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_DEBBUGER_TAB);

		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);

		Composite comboComp = new Composite(comp, SWT.NONE);
		layout = new GridLayout(2, false);
		comboComp.setLayout(layout);
		Label dlabel = new Label(comboComp, SWT.NONE);
		dlabel.setText(LaunchUIPlugin.getResourceString("Launch.common.DebuggerColon")); //$NON-NLS-1$
		fDCombo = new Combo(comboComp, SWT.DROP_DOWN | SWT.READ_ONLY);
		fDCombo.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				updateComboFromSelection();
			}
		});

		Composite radioComp = new Composite(comp, SWT.NONE);
		GridLayout radioLayout = new GridLayout(2, true);
		radioLayout.marginHeight = 0;
		radioLayout.marginWidth = 0;
		radioComp.setLayout(radioLayout);
		fRunButton = createRadioButton(radioComp, LaunchUIPlugin.getResourceString("CDebuggerTab.Run_program_in_debugger")); //$NON-NLS-1$
		fRunButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				if (fRunButton.getSelection() == true) {
					fStopInMain.setEnabled(true);
				} else {
					fStopInMain.setEnabled(false);
				}
				updateLaunchConfigurationDialog();
			}
		});
		fAttachButton = createRadioButton(radioComp, LaunchUIPlugin.getResourceString("CDebuggerTab.Attach_to_running_process")); //$NON-NLS-1$
		fAttachButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		Composite optionComp = new Composite(comp, SWT.NONE);
		layout = new GridLayout(2, false);
		optionComp.setLayout(layout);
		gd = new GridData();
		gd.horizontalSpan = 2;
		optionComp.setLayoutData(gd);

		fStopInMain = new Button(optionComp, SWT.CHECK);
		fStopInMain.setText(LaunchUIPlugin.getResourceString("CDebuggerTab.Stop_at_main_on_startup")); //$NON-NLS-1$
		fStopInMain.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		fVarBookKeeping = new Button(optionComp, SWT.CHECK);
		fVarBookKeeping.setText(LaunchUIPlugin.getResourceString("CDebuggerTab.Automatically_track_values_of_variables")); //$NON-NLS-1$
		fVarBookKeeping.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		Group debuggerGroup = new Group(comp, SWT.SHADOW_ETCHED_IN);
		debuggerGroup.setText(LaunchUIPlugin.getResourceString("CDebuggerTab.Debugger_Options")); //$NON-NLS-1$
		setDynamicTabHolder(debuggerGroup);
		GridLayout tabHolderLayout = new GridLayout();
		tabHolderLayout.marginHeight = 0;
		tabHolderLayout.marginWidth = 0;
		tabHolderLayout.numColumns = 1;
		getDynamicTabHolder().setLayout(tabHolderLayout);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		getDynamicTabHolder().setLayoutData(gd);
	}

	protected void loadDebuggerComboBox(ILaunchConfiguration config, String selection) {
		ICDebugConfiguration[] debugConfigs;
		String configPlatform = getPlatform(config);
		String programCPU = ICDebugConfiguration.CPU_NATIVE;
		ICElement ce = getContext(config, configPlatform);
		if (ce instanceof IBinary) {
			IBinary bin = (IBinary) ce;
			programCPU = bin.getCPU();
		}
		fDCombo.removeAll();
		debugConfigs = CDebugCorePlugin.getDefault().getDebugConfigurations();
		Arrays.sort(debugConfigs, new Comparator() {

			public int compare(Object o1, Object o2) {
				ICDebugConfiguration ic1 = (ICDebugConfiguration) o1;
				ICDebugConfiguration ic2 = (ICDebugConfiguration) o2;
				return ic1.getName().compareTo(ic2.getName());
			}
		});
		int x = 0;
		int selndx = -1;
		for (int i = 0; i < debugConfigs.length; i++) {
			if (debugConfigs[i].supportsMode(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN)
					|| debugConfigs[i].supportsMode(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH)) {
				String debuggerPlatform = debugConfigs[i].getPlatform();
				if (debuggerPlatform.equalsIgnoreCase(configPlatform)
						|| (debuggerPlatform.equalsIgnoreCase("*"))) { //$NON-NLS-1$
					if (debugConfigs[i].supportsCPU(programCPU)) {
						fDCombo.add(debugConfigs[i].getName());
						fDCombo.setData(Integer.toString(x), debugConfigs[i]);
						// select first exact matching debugger for platform or requested selection
						if ((selndx == -1 && debuggerPlatform.equalsIgnoreCase(configPlatform))
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

		pageUpdated = false;
		fDCombo.select(selndx == -1 ? 0 : selndx);
		//The behaviour is undefined for if the callbacks should be triggered for this,
		//so force page update if needed.
		if (!pageUpdated) {
			updateComboFromSelection();
		}
		pageUpdated = false;
		getControl().getParent().layout(true);
	}

	protected void updateComboFromSelection() {
		pageUpdated = true;
		handleDebuggerChanged();
		ICDebugConfiguration debugConfig = getConfigForCurrentDebugger();
		if (debugConfig != null) {
			fRunButton.setEnabled(debugConfig.supportsMode(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN));
			fRunButton.setSelection(false);
			fAttachButton.setEnabled(debugConfig.supportsMode(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH));
			fAttachButton.setSelection(false);
			try {
				String mode = getLaunchConfiguration().getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
						ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
				if (mode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN) && fRunButton.isEnabled()) {
					fRunButton.setSelection(true);
				} else if (mode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH) && fAttachButton.isEnabled()) {
					fAttachButton.setSelection(true);
				}
				if (fRunButton.getSelection() == true) {
					fStopInMain.setEnabled(true);
				} else {
					fStopInMain.setEnabled(false);
				}
			} catch (CoreException ex) {
			}
		}
		updateLaunchConfigurationDialog();
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		super.setDefaults(config);
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, DEFAULT_STOP_AT_MAIN);
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING, false);
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
				ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
	}

	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		super.activated(workingCopy);
		try {
			String id = workingCopy.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, ""); //$NON-NLS-1$
			loadDebuggerComboBox(workingCopy, id);
		} catch (CoreException e) {
		}
	}

	public void initializeFrom(ILaunchConfiguration config) {
		super.initializeFrom(config);
		try {
			String id = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, ""); //$NON-NLS-1$
			loadDebuggerComboBox(config, id);
			String mode = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
			if (mode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN)) {
				fRunButton.setSelection(true);
				fAttachButton.setSelection(false);
			} else if (mode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH)) {
				fAttachButton.setSelection(true);
				fRunButton.setSelection(false);
			}
			if (config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, DEFAULT_STOP_AT_MAIN) == true) {
				fStopInMain.setSelection(true);
			}
			if (config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING, false) == false) {
				fVarBookKeeping.setSelection(true);
			}
		} catch (CoreException e) {
			return;
		}
	}

	public void performApply(ILaunchConfigurationWorkingCopy config) {
		if (isValid(config)) {
			super.performApply(config);
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, false);
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING,
					!fVarBookKeeping.getSelection());
			if (fAttachButton.getSelection() == true) {
				config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
						ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH);
			} else {
				config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, fStopInMain.getSelection());
				config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
						ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
			}
		}
	}

	public boolean isValid(ILaunchConfiguration config) {
		if (!validateDebuggerConfig(config)) {
			setErrorMessage(LaunchUIPlugin.getResourceString("CDebuggerTab.No_debugger_available")); //$NON-NLS-1$
			return false;
		}
		if (super.isValid(config) == false) {
			return false;
		}
		if (!fRunButton.getSelection() && !fAttachButton.getSelection()) {
			setErrorMessage(LaunchUIPlugin.getResourceString("CDebuggerTab.Select_Debug_mode")); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	private boolean validateDebuggerConfig(ILaunchConfiguration config) {
		ICElement ce = getContext(config, null);
		String projectPlatform = getPlatform(config);
		String projectCPU = ICDebugConfiguration.CPU_NATIVE;
		if (ce != null) {
			if (ce instanceof IBinary) {
				IBinary bin = (IBinary) ce;
				projectCPU = bin.getCPU();
			}
		}
		ICDebugConfiguration debugConfig = getDebugConfig();
		if (debugConfig == null) {
			return false;
		}
		String debuggerPlatform = debugConfig.getPlatform();
		if (debuggerPlatform.equalsIgnoreCase(projectPlatform)
				|| (debuggerPlatform.equalsIgnoreCase("*"))) { //$NON-NLS-1$
			if (debugConfig.supportsCPU(projectCPU)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return the class that implements <code>ILaunchConfigurationTab</code> that is registered against the debugger id of the
	 * currently selected debugger.
	 */
	protected ICDebugConfiguration getConfigForCurrentDebugger() {
		int selectedIndex = fDCombo.getSelectionIndex();
		return (ICDebugConfiguration) fDCombo.getData(Integer.toString(selectedIndex));
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#updateLaunchConfigurationDialog()
	 */
	protected void updateLaunchConfigurationDialog() {
		super.updateLaunchConfigurationDialog();
	}

}