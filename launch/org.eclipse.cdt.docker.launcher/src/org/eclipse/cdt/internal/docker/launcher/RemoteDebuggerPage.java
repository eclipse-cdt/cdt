/*******************************************************************************
 * Copyright (c) 2006, 2010, 2015 PalmSource, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Ewa Matejska (PalmSource)
 * 
 * Referenced GDBDebuggerPage code to write this.
 * Anna Dushistova (Mentor Graphics) - moved to org.eclipse.cdt.launch.remote.tabs
 * Red Hat Inc. - modified to work with CDT Container Launcher
 *******************************************************************************/

package org.eclipse.cdt.internal.docker.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 * The dynamic debugger tab for Docker Container launches using gdb server. The
 * gdbserver settings are used to start a gdbserver session in the Docker
 * Container and then to connect to it from the host.
 */
public class RemoteDebuggerPage extends GdbDebuggerPage {

	protected Text fGDBServerCommandText;

	protected Text fGDBServerPortNumberText;

	@Override
	public String getName() {
		return Messages.Remote_GDB_Debugger_Options;
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		super.setDefaults(configuration);
		configuration.setAttribute(ILaunchConstants.ATTR_GDBSERVER_COMMAND,
				ILaunchConstants.ATTR_GDBSERVER_COMMAND_DEFAULT);
		configuration.setAttribute(ILaunchConstants.ATTR_GDBSERVER_PORT,
				ILaunchConstants.ATTR_GDBSERVER_PORT_DEFAULT);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);
		String gdbserverCommand = null;
		String gdbserverPortNumber = null;
		try {
			gdbserverCommand = configuration.getAttribute(
					ILaunchConstants.ATTR_GDBSERVER_COMMAND,
					ILaunchConstants.ATTR_GDBSERVER_COMMAND_DEFAULT);
		} catch (CoreException e) {
		}
		try {
			gdbserverPortNumber = configuration.getAttribute(
					ILaunchConstants.ATTR_GDBSERVER_PORT,
					ILaunchConstants.ATTR_GDBSERVER_PORT_DEFAULT);
		} catch (CoreException e) {
		}
		fGDBServerCommandText.setText(gdbserverCommand);
		fGDBServerPortNumberText.setText(gdbserverPortNumber);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);
		String str = fGDBServerCommandText.getText();
		str.trim();
		configuration
				.setAttribute(ILaunchConstants.ATTR_GDBSERVER_COMMAND, str);
		str = fGDBServerPortNumberText.getText();
		str.trim();
		configuration.setAttribute(ILaunchConstants.ATTR_GDBSERVER_PORT, str);
	}

	protected void createGdbserverSettingsTab(TabFolder tabFolder) {
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(Messages.Gdbserver_Settings_Tab_Name);

		Composite comp = new Composite(tabFolder, SWT.NULL);
		comp.setLayout(new GridLayout(1, true));
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		((GridLayout) comp.getLayout()).makeColumnsEqualWidth = false;
		comp.setFont(tabFolder.getFont());
		tabItem.setControl(comp);

		Composite subComp = new Composite(comp, SWT.NULL);
		subComp.setLayout(new GridLayout(2, true));
		subComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		((GridLayout) subComp.getLayout()).makeColumnsEqualWidth = false;
		subComp.setFont(tabFolder.getFont());

		Label label = new Label(subComp, SWT.LEFT);
		label.setText(Messages.Gdbserver_name_textfield_label);
		GridData gd = new GridData();
		label.setLayoutData(gd);

		fGDBServerCommandText = new Text(subComp, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData();
		fGDBServerCommandText.setLayoutData(data);
		fGDBServerCommandText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		label = new Label(subComp, SWT.LEFT);
		label.setText(Messages.Port_number_textfield_label);
		gd = new GridData();
		label.setLayoutData(gd);

		fGDBServerPortNumberText = new Text(subComp, SWT.SINGLE | SWT.BORDER);
		data = new GridData();
		fGDBServerPortNumberText.setLayoutData(data);
		fGDBServerPortNumberText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.debug.mi.internal.ui.GDBDebuggerPage#createTabs(org.eclipse
	 * .swt.widgets.TabFolder)
	 */
	@Override
	public void createTabs(TabFolder tabFolder) {
		super.createTabs(tabFolder);
		createGdbserverSettingsTab(tabFolder);
	}
}
