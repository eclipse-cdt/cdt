/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.launch.ui;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.launch.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.CLaunchConfigurationTab;
import org.eclipse.cdt.launch.internal.ui.LaunchImages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
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

public class CDebuggerTab extends CLaunchConfigurationTab {
	protected Combo fDCombo;
	protected Button fStopInMain;
	protected Button fAttachButton;
	protected Button fCoreButton;
	protected Button fRunButton;

	// Dynamic Debugger UI widgets
	protected ILaunchConfigurationTab fDynamicTab;
	protected Composite fDynamicTabHolder;
	protected ICDebugConfiguration fCurrentDebugConfig;

	protected ILaunchConfigurationWorkingCopy fWorkingCopy;
	protected ILaunchConfiguration fLaunchConfiguration;

	public void createControl(Composite parent) {
		GridData gd;

		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		GridLayout topLayout = new GridLayout(2, false);
		comp.setLayout(topLayout);
		Label dlabel = new Label(comp, SWT.NONE);
		dlabel.setText("Debugger:");
		fDCombo = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
		fDCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleDebuggerComboBoxModified();
			}
		});
		Composite radioComp = new Composite(comp, SWT.NONE);
		GridLayout radioLayout = new GridLayout(3, true);
		radioLayout.marginHeight = 0;
		radioLayout.marginWidth = 0;
		radioComp.setLayout(radioLayout);
		gd = new GridData();
		gd.horizontalSpan = 2;
		radioComp.setLayoutData(gd);
		fRunButton = createRadioButton(radioComp, "Run program in debugger.");
		fRunButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if ( fRunButton.getSelection() == true ) {
					fStopInMain.setEnabled(true);
				} else {
					fStopInMain.setEnabled(false);
				}
				updateLaunchConfigurationDialog();
			}
		});
		fAttachButton = createRadioButton(radioComp, "Attach to running process.");
		fAttachButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		fCoreButton = createRadioButton(radioComp, "View Corefile.");
		fCoreButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		fStopInMain = new Button(comp, SWT.CHECK);
		fStopInMain.setText("Stop at main() on startup.");
		gd = new GridData();
		gd.horizontalSpan = 2;
		fStopInMain.setLayoutData(gd);

		Group debuggerGroup = new Group(comp, SWT.SHADOW_ETCHED_IN);
		debuggerGroup.setText("Debugger Options");
		setDynamicTabHolder(debuggerGroup);
		GridLayout tabHolderLayout = new GridLayout();
		tabHolderLayout.marginHeight = 0;
		tabHolderLayout.marginWidth = 0;
		tabHolderLayout.numColumns = 1;
		getDynamicTabHolder().setLayout(tabHolderLayout);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		getDynamicTabHolder().setLayoutData(gd);
		LaunchUIPlugin.setDebugDialogShell(parent.getShell());
	}

	protected void setDynamicTabHolder(Composite tabHolder) {
		this.fDynamicTabHolder = tabHolder;
	}

	protected Composite getDynamicTabHolder() {
		return fDynamicTabHolder;
	}

	protected void setDynamicTab(ILaunchConfigurationTab tab) {
		fDynamicTab = tab;
	}

	protected ILaunchConfigurationTab getDynamicTab() {
		return fDynamicTab;
	}

	protected ICDebugConfiguration getDebugConfig() {
		return fCurrentDebugConfig;
	}

	protected void setDebugConfig(ICDebugConfiguration config) {
		fCurrentDebugConfig = config;
	}
	/**
	 * Notification that the user changed the selection in the JRE combo box.
	 */
	protected void handleDebuggerComboBoxModified() {
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

	protected void loadDebuggerComboBox(ILaunchConfiguration config, String selection) {
		ICDebugConfiguration[] debugConfigs;
		String platform = getPlatform(config);
		fDCombo.removeAll();
		debugConfigs = CDebugCorePlugin.getDefault().getDebugConfigurations();
		int x = 0;
		int selndx = 0;
		for (int i = 0; i < debugConfigs.length; i++) {
			if (debugConfigs[i].supportsMode(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN)
				|| debugConfigs[i].supportsMode(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH)
				|| debugConfigs[i].supportsMode(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE)) {
				String supported[] = debugConfigs[i].getPlatforms();
				for (int j = 0; j < supported.length; j++) {
					if (supported[j].equals("*") || supported[j].equalsIgnoreCase(platform)) {
						fDCombo.add(debugConfigs[i].getName());
						fDCombo.setData(Integer.toString(x), debugConfigs[i]);
						if (selection.equals(debugConfigs[i].getID())) {
							selndx = x;
						}
						x++;
						break;
					}
				}
			}
		}
		fDCombo.select(selndx);
		fDCombo.getParent().layout(true);
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		setLaunchConfigurationWorkingCopy(config);
		ILaunchConfigurationTab dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			dynamicTab.setDefaults(config);
		}
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, false);
		config.setAttribute(
			ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
			ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
	}

	public void initializeFrom(ILaunchConfiguration config) {
		String id;

		setLaunchConfiguration(config);
		try {
			id = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, "");
			loadDebuggerComboBox(config, id);
			ILaunchConfigurationTab dynamicTab = getDynamicTab();
			if (dynamicTab != null) {
				dynamicTab.initializeFrom(config);
			}
			if (config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, false) == true) {
				fStopInMain.setSelection(true);
			}
		}
		catch (CoreException e) {
			return;
		}
	}

	public void performApply(ILaunchConfigurationWorkingCopy config) {
		if (isValid(config)) {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, getDebugConfig().getID());
			ILaunchConfigurationTab dynamicTab = getDynamicTab();
			if (dynamicTab == null) {
				config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_SPECIFIC_ATTRS_MAP, (Map) null);
			}
			else {
				dynamicTab.performApply(config);
			}
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, false);
			if (fAttachButton.getSelection() == true) {
				config.setAttribute(
					ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH);
			}
			else if (fCoreButton.getSelection() == true) {
				config.setAttribute(
					ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE);
			}
			else {
				config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, fStopInMain.getSelection());
				config.setAttribute(
					ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
			}
		}
	}

	public boolean isValid(ILaunchConfiguration config) {
		setErrorMessage(null);
		setMessage(null);

		if (fDCombo.getSelectionIndex() == -1) {
			setErrorMessage("No debugger avalible");
			return false;
		}
		if ( !fRunButton.getSelection() 
			&& !fAttachButton.getSelection()
			&& !fCoreButton.getSelection() ) {
				setErrorMessage("Select a Debug mode.");
				return false;
		}
		ILaunchConfigurationTab dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			return dynamicTab.isValid(config);
		}
		return true;
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
		if (getDynamicTab() == null) {
			return;
		}
		setDebugConfig(debugConfig);
		// Ask the dynamic UI to create its Control
		getDynamicTab().setLaunchConfigurationDialog(getLaunchConfigurationDialog());
		getDynamicTab().createControl(getDynamicTabHolder());
		getDynamicTab().getControl().setVisible(true);
		getDynamicTabHolder().layout(true);

		fRunButton.setEnabled(debugConfig.supportsMode(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN));
		fRunButton.setSelection(false);
		fAttachButton.setEnabled(debugConfig.supportsMode(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH));
		fAttachButton.setSelection(false);
		fCoreButton.setEnabled(debugConfig.supportsMode(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE));
		fCoreButton.setSelection(false);
		try {
			String mode =
				getLaunchConfiguration().getAttribute(
					ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
			if (mode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN) && fRunButton.isEnabled()) {
				fRunButton.setSelection(true);
			}
			else if (mode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH) && fAttachButton.isEnabled()) {
				fAttachButton.setSelection(true);
			}
			else if (mode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE) && fCoreButton.isEnabled()) {
				fCoreButton.setSelection(true);
			}
			if ( fRunButton.getSelection() == true ) {
				fStopInMain.setEnabled(true);
			} else {
				fStopInMain.setEnabled(false);
			}
		}
		catch (CoreException e) {
		}
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

	protected void setLaunchConfigurationWorkingCopy(ILaunchConfigurationWorkingCopy workingCopy) {
		fWorkingCopy = workingCopy;
	}

	protected ILaunchConfiguration getLaunchConfiguration() {
		return fLaunchConfiguration;
	}

	protected void setLaunchConfiguration(ILaunchConfiguration launchConfiguration) {
		fLaunchConfiguration = launchConfiguration;
	}

	protected ILaunchConfigurationWorkingCopy getLaunchConfigurationWorkingCopy() {
		return fWorkingCopy;
	}

	public String getName() {
		return "Debugger";
	}

	public Image getImage() {
		return LaunchImages.get(LaunchImages.IMG_VIEW_DEBUGGER_TAB);
	}
}
