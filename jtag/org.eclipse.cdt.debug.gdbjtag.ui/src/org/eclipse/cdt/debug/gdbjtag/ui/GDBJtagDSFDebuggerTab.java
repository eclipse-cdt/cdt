/*******************************************************************************
 * Copyright (c) 2007, 2020 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Andy Jin - Hardware debugging UI improvements, bug 229946
 *     Anna Dushistova (MontaVista) - bug 241279
 *              - Hardware Debugging: Host name or ip address not saving in
 *                the debug configuration
 *     Andy Jin (QNX) - Added DSF debugging, bug 248593
 *     Bruce Griffith, Sage Electronic Engineering, LLC - bug 305943
 *              - API generalization to become transport-independent (e.g. to
 *                allow connections via serial ports and pipes).
 *     John Dallaway - Ensure correct SessionType enabled, bug 334110
 *     Torbjörn Svensson (STMicroelectronics) - Bug 535024
 *     John Dallaway - Sort JTAG device list, bug 560186
 *     John Dallaway - Eliminate deprecated API, bug 566462
*******************************************************************************/

package org.eclipse.cdt.debug.gdbjtag.ui;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.gdbjtag.core.Activator;
import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConnection;
import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConstants;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.GDBJtagDeviceContribution;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.GDBJtagDeviceContributionFactory;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.launching.ICDTLaunchHelpContextIds;
import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * @since 7.0
 */
public class GDBJtagDSFDebuggerTab extends AbstractLaunchConfigurationTab {

	private static final String TAB_NAME = Messages.getString("GDBJtagDebuggerTab.tabName"); //$NON-NLS-1$
	private static final String TAB_ID = "org.eclipse.cdt.debug.gdbjtag.ui.debuggertab.dsf"; //$NON-NLS-1$
	private static final String DEFAULT_JTAG_DEVICE_ID = "org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.genericDevice"; //$NON-NLS-1$
	private static final String ATTR_JTAG_DEVICE = "org.eclipse.cdt.debug.gdbjtag.core.jtagDevice"; //$NON-NLS-1$

	private Text gdbCommand;
	private Button useRemote;
	private Combo jtagDevice;
	private Composite remoteConnectionParameters;
	private StackLayout remoteConnectParmsLayout;
	private Composite remoteConnectionBox;
	private Text connection;
	private String savedJtagDevice;
	protected Button fUpdateThreadlistOnSuspend;
	private Button remoteTimeoutEnabled;
	private Text remoteTimeoutValue;

	@Override
	public String getName() {
		return TAB_NAME;
	}

	@Override
	public Image getImage() {
		return GDBJtagImages.getDebuggerTabImage();
	}

	@Override
	public void createControl(Composite parent) {
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		setControl(sc);

		Composite comp = new Composite(sc, SWT.NONE);
		sc.setContent(comp);
		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);

		Group group = new Group(comp, SWT.NONE);
		layout = new GridLayout();
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		group.setLayoutData(gd);
		group.setText(Messages.getString("GDBJtagDebuggerTab.gdbSetupGroup_Text")); //$NON-NLS-1$

		createCommandControl(group);
		createRemoteControl(comp);

		fUpdateThreadlistOnSuspend = new Button(comp, SWT.CHECK);
		fUpdateThreadlistOnSuspend.setText(Messages.getString("GDBJtagDebuggerTab.update_thread_list_on_suspend")); //$NON-NLS-1$
		fUpdateThreadlistOnSuspend.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		gd = new GridData();
		gd.horizontalSpan = 2;
		fUpdateThreadlistOnSuspend.setLayoutData(gd);

		// This checkbox needs an explanation. Attach context help to it.
		PlatformUI.getWorkbench().getHelpSystem().setHelp(fUpdateThreadlistOnSuspend,
				"org.eclipse.cdt.dsf.gdb.ui.update_threadlist_button_context"); //$NON-NLS-1$
		// Attach context help to this tab.
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				ICDTLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_DEBBUGER_TAB);
	}

	private void browseButtonSelected(String title, Text text) {
		FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
		dialog.setText(title);
		String str = text.getText().trim();
		int lastSeparatorIndex = str.lastIndexOf(File.separator);
		if (lastSeparatorIndex != -1)
			dialog.setFilterPath(str.substring(0, lastSeparatorIndex));
		str = dialog.open();
		if (str != null)
			text.setText(str);
	}

	private void variablesButtonSelected(Text text) {
		StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
		if (dialog.open() == StringVariableSelectionDialog.OK) {
			text.insert(dialog.getVariableExpression());
		}
	}

	private void createCommandControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		comp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		comp.setLayoutData(gd);

		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("GDBJtagDebuggerTab.gdbCommandLabel")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 3;
		label.setLayoutData(gd);

		gdbCommand = new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gdbCommand.setLayoutData(gd);
		gdbCommand.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				scheduleUpdateJob(); // provides much better performance for Text listeners
			}
		});

		Button button = new Button(comp, SWT.NONE);
		button.setText(Messages.getString("GDBJtagDebuggerTab.gdbCommandBrowse")); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browseButtonSelected(Messages.getString("GDBJtagDebuggerTab.gdbCommandBrowse_Title"), gdbCommand); //$NON-NLS-1$
			}
		});

		button = new Button(comp, SWT.NONE);
		button.setText(Messages.getString("GDBJtagDebuggerTab.gdbCommandVariable")); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				variablesButtonSelected(gdbCommand);
			}
		});
	}

	private void createRemoteControl(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		group.setLayoutData(gd);
		group.setText(Messages.getString("GDBJtagDebuggerTab.remoteGroup_Text")); //$NON-NLS-1$

		useRemote = new Button(group, SWT.CHECK);
		useRemote.setLayoutData(GridDataFactory.swtDefaults().span(2, 1).create());
		useRemote.setText(Messages.getString("GDBJtagDebuggerTab.useRemote_Text")); //$NON-NLS-1$
		useRemote.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				useRemoteChanged();
				updateLaunchConfigurationDialog();
			}
		});

		remoteTimeoutEnabled = new Button(group, SWT.CHECK);
		remoteTimeoutEnabled.setText(Messages.getString("GDBJtagDebuggerTab.remoteTimeout")); //$NON-NLS-1$
		remoteTimeoutEnabled.setToolTipText(Messages.getString("GDBJtagDebuggerTab.remoteTimeoutTooltip")); //$NON-NLS-1$
		remoteTimeoutEnabled.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				remoteTimeoutChanged();
				updateLaunchConfigurationDialog();
			}
		});
		remoteTimeoutValue = new Text(group, SWT.BORDER);
		gd = new GridData();
		gd.widthHint = 125;
		remoteTimeoutValue.setLayoutData(gd);
		remoteTimeoutValue.setToolTipText(Messages.getString("GDBJtagDebuggerTab.remoteTimeoutTooltip")); //$NON-NLS-1$

		Composite comp = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		comp.setLayout(layout);
		comp.setLayoutData(GridDataFactory.swtDefaults().span(2, 1).create());

		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("GDBJtagDebuggerTab.jtagDeviceLabel")); //$NON-NLS-1$

		jtagDevice = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);

		GDBJtagDeviceContribution[] availableDevices = GDBJtagDeviceContributionFactory.getInstance()
				.getGDBJtagDeviceContribution();
		Arrays.stream(availableDevices).map(GDBJtagDeviceContribution::getDeviceName).sorted()
				.forEach(name -> jtagDevice.add(name));

		jtagDevice.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateDeviceIpPort(jtagDevice.getText());
				scheduleUpdateJob(); // provides much better performance for Text listeners
			}
		});

		remoteConnectionParameters = new Composite(group, SWT.NO_TRIM | SWT.NO_FOCUS);
		remoteConnectParmsLayout = new StackLayout();
		remoteConnectionParameters.setLayout(remoteConnectParmsLayout);
		remoteConnectionParameters.setLayoutData(GridDataFactory.swtDefaults().span(2, 1).create());

		//
		//  Create entry fields for other types of connections
		//

		{
			remoteConnectionBox = new Composite(remoteConnectionParameters, SWT.NO_TRIM | SWT.NO_FOCUS);
			layout = new GridLayout();
			layout.numColumns = 2;
			remoteConnectionBox.setLayout(layout);
			remoteConnectionBox.setBackground(remoteConnectionParameters.getParent().getBackground());

			label = new Label(remoteConnectionBox, SWT.NONE);
			label.setText(Messages.getString("GDBJtagDebuggerTab.connectionLabel")); //$NON-NLS-1$
			connection = new Text(remoteConnectionBox, SWT.BORDER);
			gd = new GridData();
			gd.widthHint = 125;
			connection.setLayoutData(gd);
		}

		//
		//  Add watchers for user data entry
		//
		connection.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				scheduleUpdateJob(); // provides much better performance for Text listeners
			}
		});
	}

	/**
	 * @param text
	 */
	protected void updateDeviceIpPort(String selectedDeviceName) {
		if (selectedDeviceName.equals(savedJtagDevice)) {
			return;
		}
		GDBJtagDeviceContribution[] availableDevices = GDBJtagDeviceContributionFactory.getInstance()
				.getGDBJtagDeviceContribution();
		IGDBJtagDevice selectedDevice = null;
		for (int i = 0; i < availableDevices.length; i++) {
			String name = availableDevices[i].getDeviceName();
			if (name.equals(selectedDeviceName)) {
				selectedDevice = availableDevices[i].getDevice();
				if (selectedDevice != null) {
					if (selectedDevice instanceof IGDBJtagConnection) {
						IGDBJtagConnection connectionDevice = (IGDBJtagConnection) selectedDevice;
						connection.setText(connectionDevice.getDefaultDeviceConnection());
					}
					useRemoteChanged();
					updateLaunchConfigurationDialog();
					break;
				}
			}
		}
	}

	private void remoteTimeoutChanged() {
		remoteTimeoutValue.setEnabled(remoteTimeoutEnabled.getSelection());
	}

	private void useRemoteChanged() {
		boolean enabled = useRemote.getSelection();
		remoteTimeoutEnabled.setEnabled(enabled);
		remoteTimeoutValue.setEnabled(remoteTimeoutEnabled.getSelection());
		jtagDevice.setEnabled(enabled);
		connection.setEnabled(enabled);
		GDBJtagDeviceContribution selectedDeviceEntry = GDBJtagDeviceContributionFactory.getInstance()
				.findByDeviceName(jtagDevice.getText());
		if ((selectedDeviceEntry == null) || (selectedDeviceEntry.getDevice() == null)) {
			remoteConnectParmsLayout.topControl = null;
			remoteConnectionParameters.layout();
		} else {
			IGDBJtagDevice device = selectedDeviceEntry.getDevice();
			if (device instanceof IGDBJtagConnection) {
				remoteConnectParmsLayout.topControl = remoteConnectionBox;
				remoteConnectionBox.getParent().layout();
			} else {
				remoteConnectParmsLayout.topControl = null;
				remoteConnectionParameters.layout();
			}
		}
	}

	/**
	 * Returns the device name for a given device id or {@link IGDBJtagConstants.DEFAULT_JTAG_DEVICE_NAME}
	 *
	 * @param jtagDeviceId The device id
	 * @return The device id if found, else {@link IGDBJtagConstants.DEFAULT_JTAG_DEVICE_NAME}
	 * @since 8.1
	 */
	protected String getDeviceNameForDeviceId(String jtagDeviceId) {
		GDBJtagDeviceContribution contribution = GDBJtagDeviceContributionFactory.getInstance()
				.findByDeviceId(jtagDeviceId);
		if (contribution != null) {
			return contribution.getDeviceName();
		}
		return IGDBJtagConstants.DEFAULT_JTAG_DEVICE_NAME;
	}

	/**
	 * Returns the device id for a given device name or {@link #DEFAULT_JTAG_DEVICE_ID}
	 *
	 * @param jtagDeviceName The device name
	 * @return The device id if found, else {@link #DEFAULT_JTAG_DEVICE_ID}
	 * @since 8.1
	 */
	protected String getDeviceIdForDeviceName(String jtagDeviceName) {
		GDBJtagDeviceContribution device = GDBJtagDeviceContributionFactory.getInstance()
				.findByDeviceName(jtagDeviceName);
		if (device != null) {
			return device.getDeviceId();
		}
		return DEFAULT_JTAG_DEVICE_ID;
	}

	private GDBJtagDeviceContribution getDeviceContribution(ILaunchConfiguration configuration) throws CoreException {
		String deviceId = configuration.getAttribute(IGDBJtagConstants.ATTR_JTAG_DEVICE_ID, (String) null);
		if (deviceId != null) {
			return GDBJtagDeviceContributionFactory.getInstance().findByDeviceId(deviceId);
		}

		// Fall back to old behavior with name only if ID is missing
		String deviceName = configuration.getAttribute(ATTR_JTAG_DEVICE, (String) null);
		if (deviceName != null) {
			return GDBJtagDeviceContributionFactory.getInstance().findByDeviceName(deviceName);
		}

		// No matching device contribution found
		return null;
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			String defaultGdbCommand = Platform.getPreferencesService().getString(GdbPlugin.PLUGIN_ID,
					IGdbDebugPreferenceConstants.PREF_DEFAULT_GDB_COMMAND, "", null); //$NON-NLS-1$
			String gdbCommandAttr = configuration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME,
					defaultGdbCommand);
			gdbCommand.setText(gdbCommandAttr);

			boolean useRemoteAttr = configuration.getAttribute(IGDBJtagConstants.ATTR_USE_REMOTE_TARGET,
					IGDBJtagConstants.DEFAULT_USE_REMOTE_TARGET);
			useRemote.setSelection(useRemoteAttr);

			GDBJtagDeviceContribution savedDeviceContribution = getDeviceContribution(configuration);
			if (savedDeviceContribution != null) {
				savedJtagDevice = savedDeviceContribution.getDeviceName();
			} else {
				savedJtagDevice = IGDBJtagConstants.DEFAULT_JTAG_DEVICE_NAME;
			}

			if (savedJtagDevice.isEmpty()) {
				// saved device not known so use default device attributes
				String deviceName = getDeviceNameForDeviceId(DEFAULT_JTAG_DEVICE_ID);
				int index = 0;
				for (int i = 0; i < jtagDevice.getItemCount(); i++) {
					if (jtagDevice.getItem(i).equals(deviceName)) {
						index = i;
						break;
					}
				}
				jtagDevice.select(index);
			} else {
				String storedAddress = ""; //$NON-NLS-1$
				int storedPort = -1;
				String storedConnection = null;

				for (int i = 0; i < jtagDevice.getItemCount(); i++) {
					if (jtagDevice.getItem(i).equals(savedJtagDevice)) {
						storedAddress = configuration.getAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS, ""); //$NON-NLS-1$
						storedPort = configuration.getAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER, -1);
						storedConnection = configuration.getAttribute(IGDBJtagConstants.ATTR_CONNECTION, (String) null);
						jtagDevice.select(i);
						break;
					}
				}

				String connectionText = IGDBJtagConstants.DEFAULT_CONNECTION;
				if (null != storedConnection) { // if a connection URI
					try {
						connectionText = new URI(storedConnection).getSchemeSpecificPart();
					} catch (URISyntaxException e) {
						Activator.log(e);
					}
				} else if (storedPort != -1) { // if a legacy address:port
					connectionText = String.format("%s:%d", storedAddress, storedPort); //$NON-NLS-1$
				}
				connection.setText(connectionText);
			}
			boolean updateThreadsOnSuspend = configuration.getAttribute(
					IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND,
					IGDBLaunchConfigurationConstants.DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND_DEFAULT);
			fUpdateThreadlistOnSuspend.setSelection(updateThreadsOnSuspend);
			remoteTimeoutEnabled.setSelection(
					configuration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_TIMEOUT_ENABLED,
							LaunchUtils.getRemoteTimeoutEnabledDefault()));
			remoteTimeoutValue.setText(
					configuration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_TIMEOUT_VALUE,
							LaunchUtils.getRemoteTimeoutValueDefault()));

			remoteTimeoutChanged();
			useRemoteChanged();
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 */
	@Override
	public String getId() {
		return TAB_ID;
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME, gdbCommand.getText().trim()); // DSF
		savedJtagDevice = jtagDevice.getText();
		configuration.setAttribute(IGDBJtagConstants.ATTR_JTAG_DEVICE_ID, getDeviceIdForDeviceName(savedJtagDevice));
		configuration.setAttribute(IGDBJtagConstants.ATTR_USE_REMOTE_TARGET, useRemote.getSelection());
		if (useRemote.getSelection()) {
			// ensure LaunchUtils.getSessionType() returns SessionType.REMOTE (bug 334110)
			configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE);
		} else {
			// ensure LaunchUtils.getSessionType() returns the default session type
			configuration.removeAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE);
		}
		if (!savedJtagDevice.isEmpty()) {
			try {
				IGDBJtagDevice device = GDBJtagDeviceContributionFactory.getInstance().findByDeviceName(savedJtagDevice)
						.getDevice();
				if (device instanceof IGDBJtagConnection) {
					String conn = connection.getText().trim();
					URI uri = new URI("gdb", conn, ""); //$NON-NLS-1$ //$NON-NLS-2$
					configuration.setAttribute(IGDBJtagConstants.ATTR_CONNECTION, uri.toString());
					configuration.removeAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS);
					configuration.removeAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER);
				} else {
					configuration.removeAttribute(IGDBJtagConstants.ATTR_CONNECTION);
				}
			} catch (URISyntaxException e) {
				Activator.log(e);
			} catch (NumberFormatException e) {
				Activator.log(e);
			}
		}
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND,
				fUpdateThreadlistOnSuspend.getSelection());
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_TIMEOUT_ENABLED,
				remoteTimeoutEnabled.getSelection());
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_TIMEOUT_VALUE,
				remoteTimeoutValue.getText().trim());
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		String defaultGdbCommand = Platform.getPreferencesService().getString(GdbPlugin.PLUGIN_ID,
				IGdbDebugPreferenceConstants.PREF_DEFAULT_GDB_COMMAND, "", null); //$NON-NLS-1$
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME, defaultGdbCommand);

		configuration.setAttribute(IGDBJtagConstants.ATTR_USE_REMOTE_TARGET,
				IGDBJtagConstants.DEFAULT_USE_REMOTE_TARGET);
		configuration.setAttribute(IGDBJtagConstants.ATTR_JTAG_DEVICE_ID, DEFAULT_JTAG_DEVICE_ID);
		configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
				IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE);
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND,
				IGDBLaunchConfigurationConstants.DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND_DEFAULT);
	}

}
