/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.launch.ui;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.launch.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.AbstractCDebuggerTab;
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

public class CorefileDebuggerTab extends AbstractCDebuggerTab {
	protected Combo fDCombo;

	private boolean initializingComboBox = false;

	public void createControl(Composite parent) {
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
			}
		});
		Group debuggerGroup = new Group(comp, SWT.SHADOW_ETCHED_IN);
		debuggerGroup.setText("Debugger Options");
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
		String projectPlatform = "native";
		String projectCPU = "native";
		if (ce != null) {
			try {
				ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(ce.getCProject().getProject());
				projectPlatform = descriptor.getPlatform();
				IBinary bin = (IBinary) ce;
				projectCPU = bin.getCPU();
			}
			catch (Exception e) {
			}
		}
		fDCombo.removeAll();
		debugConfigs = CDebugCorePlugin.getDefault().getDebugConfigurations();
		int x = 0;
		int selndx = -1;
		for (int i = 0; i < debugConfigs.length; i++) {
			if (debugConfigs[i].supportsMode(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE)) {
				String debuggerPlatform = debugConfigs[i].getPlatform();
				boolean isNative = configPlatform.equals(projectPlatform);
				if (debuggerPlatform.equalsIgnoreCase(projectPlatform)
					|| (isNative && debuggerPlatform.equalsIgnoreCase("native"))) {
					if (debugConfigs[i].supportsCPU(projectCPU)) {
						fDCombo.add(debugConfigs[i].getName());
						fDCombo.setData(Integer.toString(x), debugConfigs[i]);
						// select first exact matching debugger for platform or requested selection
						if ((selndx == -1 && debuggerPlatform.equalsIgnoreCase(projectPlatform)) ||
							selection.equals(debugConfigs[i].getID())) {
							selndx = x;
						}
						x++;
					}
				}
			}
		}
		// if no selection meaning nothing in config the force initdefault on tab
		setInitializeDefault(selection.equals("") ? true : false);

		fDCombo.select(selndx == -1 ? 0 : selndx);
		//The behaviour is undefined for if the callbacks should be triggered for this,
		//so to avoid unnecessary confusion, we force an update.
		handleDebuggerChanged();
		fDCombo.getParent().layout(true);
		initializingComboBox = false;
	}

	public void initializeFrom(ILaunchConfiguration config) {
		super.initializeFrom(config);
		try {
			String id = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, "");
			loadDebuggerComboBox(config, id);
		}
		catch (CoreException e) {
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
			setErrorMessage("No debugger available");
			return false;
		}
		return super.isValid(config);
	}

	private boolean validateDebuggerConfig(ILaunchConfiguration config) {
		String platform = getPlatform(config);
		ICElement ce = getContext(config, null);
		String projectPlatform = "native";
		String projectCPU = "native";
		if (ce != null) {
			try {
				ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(ce.getCProject().getProject());
				projectPlatform = descriptor.getPlatform();
				IBinary bin = (IBinary) ce;
				projectCPU = bin.getCPU();
			}
			catch (Exception e) {
			}
		}
		ICDebugConfiguration debugConfig = getDebugConfig();
		if (debugConfig == null) {
			return false;
		}
		String debuggerPlatform = debugConfig.getPlatform();
		boolean isNative = platform.equals(projectPlatform);
		if (debuggerPlatform.equalsIgnoreCase(projectPlatform) || (isNative && debuggerPlatform.equalsIgnoreCase("native"))) {
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

	public String getName() {
		return "Debugger";
	}

}
