/*******************************************************************************
 * Copyright (c) 2006, 2010 PalmSource, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Ewa Matejska (PalmSource)
 * Anna Dushistova (Mentor Graphics) - [314659] move remote launch/debug to DSF 
 *******************************************************************************/

package org.eclipse.cdt.launch.remote;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.launch.ui.CDebuggerTab;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

public class RemoteCDebuggerTab extends CDebuggerTab {

	private final static String DEFAULTS_SET = "org.eclipse.cdt.launch.remote.RemoteCDSFDebuggerTab.DEFAULTS_SET"; //$NON-NLS-1$
	
	public RemoteCDebuggerTab() {
		super(false);
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(getControl(),
						"org.eclipse.rse.internal.remotecdt.launchgroup"); //$NON-NLS-1$
	}

	static final private String REMOTE_GDB_DEBUGGER_NAME = "remote gdb/mi"; //$NON-NLS-1$

	public RemoteCDebuggerTab(boolean attachMode) {
		super(attachMode);
	}

	protected void loadDebuggerComboBox(ILaunchConfiguration config,
			String selection) {
		ICDebugConfiguration[] debugConfigs = CDebugCorePlugin.getDefault()
				.getDebugConfigurations();
		String defaultSelection = selection;
		List list = new ArrayList();
		for (int i = 0; i < debugConfigs.length; i++) {
			ICDebugConfiguration configuration = debugConfigs[i];
			if (configuration.getName().equals(REMOTE_GDB_DEBUGGER_NAME)) {
				list.add(configuration);
				// Select as default selection
				defaultSelection = configuration.getID();
				break;
			}
		}
		setInitializeDefault(defaultSelection.equals("") ? true : false); //$NON-NLS-1$
		loadDebuggerCombo(
				(ICDebugConfiguration[]) list.toArray(new ICDebugConfiguration[list
						.size()]), defaultSelection);
	}

	@Override
	public String getId() {
		return "org.eclipse.rse.remotecdt.launch.RemoteCDebuggerTab"; //$NON-NLS-1$
	}

	/*
	 * When the launch configuration is created for Run mode, this Debugger tab
	 * is not created because it is not used for Run mode but only for Debug
	 * mode. When we then open the same configuration in Debug mode, the launch
	 * configuration already exists and initializeFrom() is called instead of
	 * setDefaults(). We therefore call setDefaults() ourselves and update the
	 * configuration. If we don't then the user will be required to press Apply
	 * to get the default settings saved. Bug 281970
	 */
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(DEFAULTS_SET, true);
		super.setDefaults(config);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		try {
			if (config.hasAttribute(DEFAULTS_SET) == false) {
				ILaunchConfigurationWorkingCopy wc;
				wc = config.getWorkingCopy();
				setDefaults(wc);
				wc.doSave();
			}
		} catch (CoreException e) {
		}
		super.initializeFrom(config);
	}

}
