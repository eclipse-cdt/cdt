/*******************************************************************************
 * Copyright (c) 2006, 2018 PalmSource, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Ewa Matejska (PalmSource)
 *
 * Referenced GDBDebuggerPage code to write this.
 * Anna Dushistova (Mentor Graphics) - moved to org.eclipse.cdt.launch.remote.tabs
 * Red Hat Inc. - modified to work with CDT Container Launcher
 *******************************************************************************/

package org.eclipse.cdt.internal.docker.launcher;

import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
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

	protected Button fRemoteTimeoutEnabledCheckbox;

	protected Text fRemoteTimeoutValueText;

	@Override
	public String getName() {
		return Messages.Remote_GDB_Debugger_Options;
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		super.setDefaults(configuration);
		configuration.setAttribute(ILaunchConstants.ATTR_GDBSERVER_COMMAND,
				ILaunchConstants.ATTR_GDBSERVER_COMMAND_DEFAULT);
		configuration.setAttribute(ILaunchConstants.ATTR_GDBSERVER_PORT, ILaunchConstants.ATTR_GDBSERVER_PORT_DEFAULT);
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_TIMEOUT_ENABLED,
				LaunchUtils.getRemoteTimeoutEnabledDefault());
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_TIMEOUT_VALUE,
				LaunchUtils.getRemoteTimeoutValueDefault());
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);
		String gdbserverCommand = null;
		String gdbserverPortNumber = null;
		boolean remoteTimeoutEnabled = false;
		String remoteTimeoutValue = null;
		try {
			gdbserverCommand = configuration.getAttribute(ILaunchConstants.ATTR_GDBSERVER_COMMAND,
					ILaunchConstants.ATTR_GDBSERVER_COMMAND_DEFAULT);
		} catch (CoreException e) {
		}
		try {
			gdbserverPortNumber = configuration.getAttribute(ILaunchConstants.ATTR_GDBSERVER_PORT,
					ILaunchConstants.ATTR_GDBSERVER_PORT_DEFAULT);
		} catch (CoreException e) {
		}
		try {
			remoteTimeoutEnabled = configuration.getAttribute(
					IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_TIMEOUT_ENABLED,
					LaunchUtils.getRemoteTimeoutEnabledDefault());
		} catch (CoreException e) {
		}
		try {
			remoteTimeoutValue = configuration.getAttribute(
					IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_TIMEOUT_VALUE,
					LaunchUtils.getRemoteTimeoutValueDefault());
		} catch (CoreException e) {
		}
		fGDBServerCommandText.setText(gdbserverCommand);
		fGDBServerPortNumberText.setText(gdbserverPortNumber);
		fRemoteTimeoutEnabledCheckbox.setSelection(remoteTimeoutEnabled);
		fRemoteTimeoutValueText.setText(remoteTimeoutValue);
		remoteTimeoutEnabledChanged();
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);
		String str = fGDBServerCommandText.getText();
		str.trim();
		configuration.setAttribute(ILaunchConstants.ATTR_GDBSERVER_COMMAND, str);
		str = fGDBServerPortNumberText.getText();
		str.trim();
		configuration.setAttribute(ILaunchConstants.ATTR_GDBSERVER_PORT, str);
		boolean b = fRemoteTimeoutEnabledCheckbox.getSelection();
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_TIMEOUT_ENABLED, b);
		str = fRemoteTimeoutValueText.getText();
		str.trim();
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_TIMEOUT_VALUE, str);
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
		GridData data = new GridData(SWT.FILL, SWT.TOP, true, false);
		fGDBServerCommandText.setLayoutData(data);
		fGDBServerCommandText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		label = new Label(subComp, SWT.LEFT);
		label.setText(Messages.Port_number_textfield_label);
		gd = new GridData();
		label.setLayoutData(gd);

		fGDBServerPortNumberText = new Text(subComp, SWT.SINGLE | SWT.BORDER);
		data = new GridData(SWT.FILL, SWT.TOP, true, false);
		fGDBServerPortNumberText.setLayoutData(data);
		fGDBServerPortNumberText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		fRemoteTimeoutEnabledCheckbox = new Button(subComp, SWT.CHECK);
		fRemoteTimeoutEnabledCheckbox.setText(Messages.Gdbserver_Settings_Remotetimeout_label);
		fRemoteTimeoutEnabledCheckbox.setToolTipText(Messages.Gdbserver_Settings_Remotetimeout_tooltip);
		gd = new GridData();
		fRemoteTimeoutEnabledCheckbox.setLayoutData(gd);
		fRemoteTimeoutEnabledCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				remoteTimeoutEnabledChanged();
				updateLaunchConfigurationDialog();
			}
		});

		fRemoteTimeoutValueText = new Text(subComp, SWT.SINGLE | SWT.BORDER);
		data = new GridData(SWT.FILL, SWT.TOP, true, false);
		fRemoteTimeoutValueText.setLayoutData(data);
		fRemoteTimeoutValueText.setToolTipText(Messages.Gdbserver_Settings_Remotetimeout_tooltip);
		fRemoteTimeoutValueText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		remoteTimeoutEnabledChanged();
	}

	private void remoteTimeoutEnabledChanged() {
		fRemoteTimeoutValueText.setEnabled(fRemoteTimeoutEnabledCheckbox.getSelection());
	}

	@Override
	public void createTabs(TabFolder tabFolder) {
		super.createTabs(tabFolder);
		createGdbserverSettingsTab(tabFolder);
	}
}
