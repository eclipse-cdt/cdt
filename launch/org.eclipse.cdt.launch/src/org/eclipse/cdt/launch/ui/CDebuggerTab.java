/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.launch.ui;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.launch.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.AbstractCDebuggerTab;
import org.eclipse.cdt.launch.internal.ui.LaunchImages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class CDebuggerTab extends AbstractCDebuggerTab {
	protected Combo fDCombo;
	protected Button fStopInMain;
	protected Button fAttachButton;
	protected Button fRunButton;

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
				handleDebuggerChanged();
				ICDebugConfiguration debugConfig = getConfigForCurrentDebugger();
				if ( debugConfig != null ) {
					fRunButton.setEnabled(debugConfig.supportsMode(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN));
					fRunButton.setSelection(false);
					fAttachButton.setEnabled(debugConfig.supportsMode(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH));
					fAttachButton.setSelection(false);
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
						if (fRunButton.getSelection() == true) {
							fStopInMain.setEnabled(true);
						}
						else {
							fStopInMain.setEnabled(false);
						}
					}
					catch (CoreException ex) {
					}
				}
			}
		});

		Composite radioComp = new Composite(comp, SWT.NONE);
		GridLayout radioLayout = new GridLayout(2, true);
		radioLayout.marginHeight = 0;
		radioLayout.marginWidth = 0;
		radioComp.setLayout(radioLayout);
		gd = new GridData();
		gd.horizontalSpan = 2;
		radioComp.setLayoutData(gd);
		fRunButton = createRadioButton(radioComp, "Run program in debugger.");
		fRunButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (fRunButton.getSelection() == true) {
					fStopInMain.setEnabled(true);
				}
				else {
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
				|| debugConfigs[i].supportsMode(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH) ) {
				String supported[] = debugConfigs[i].getPlatforms();
				boolean isLocal = platform.equals(BootLoader.getOS());
				for (int j = 0; j < supported.length; j++) {
					if (supported[j].equalsIgnoreCase(platform) || (isLocal && supported[j].equalsIgnoreCase("local"))) {
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
		super.setDefaults(config);
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, false);
		config.setAttribute(
			ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
			ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
	}

	public void initializeFrom(ILaunchConfiguration config) {
		super.initializeFrom(config);
		try {
			String id = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, "");
			loadDebuggerComboBox(config, id);
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
			super.performApply(config);
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, false);
			if (fAttachButton.getSelection() == true) {
				config.setAttribute(
					ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH);
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
		if  (super.isValid(config) == false ) {
			return false;
		}
		if (!fRunButton.getSelection() && !fAttachButton.getSelection()) {
			setErrorMessage("Select a Debug mode.");
			return false;
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

	public String getName() {
		return "Debugger";
	}

	public Image getImage() {
		return LaunchImages.get(LaunchImages.IMG_VIEW_DEBUGGER_TAB);
	}
}
