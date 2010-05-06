/*******************************************************************************
 * Copyright (c) 2007 - 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Andy Jin - Hardware debugging UI improvements, bug 229946
 *     Anna Dushistova(MontaVista) - bug 241279 
 *              - Hardware Debugging: Host name or ip address not saving in 
 *                the debug configuration
 *     Andy Jin (QNX) - Added DSF debugging, bug 248593
 *     Bruce Griffith, Sage Electronic Engineering, LLC - bug 305943
 *              - API generalization to become transport-independent (e.g. to
 *                allow connections via serial ports and pipes).
*******************************************************************************/

package org.eclipse.cdt.debug.gdbjtag.ui;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.cdt.debug.gdbjtag.core.Activator;
import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConnection;
import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConstants;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.GDBJtagDeviceContribution;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.GDBJtagDeviceContributionFactory;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice;
import org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants;
import org.eclipse.cdt.debug.mi.core.MIPlugin;
import org.eclipse.cdt.debug.mi.core.command.factories.CommandFactoryDescriptor;
import org.eclipse.cdt.debug.mi.core.command.factories.CommandFactoryManager;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
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

/**
 * @author Doug Schaefer, Adrian Petrescu
 *
 */
public class GDBJtagDebuggerTab extends AbstractLaunchConfigurationTab {

	private static final String TAB_NAME = "Debugger";
	private static final String TAB_ID = "org.eclipse.cdt.debug.gdbjtag.ui.debuggertab.cdi";

	private CommandFactoryDescriptor[] cfDescs;
	
	private Text gdbCommand;
	private Combo commandFactory;
	private Combo miProtocol;
	private Button verboseMode;
	private Button useRemote;
	private Combo jtagDevice;
	private Composite remoteConnectionParameters;
	private StackLayout remoteConnectParmsLayout;
	private Composite remoteTcpipBox;
	private Text ipAddress;
	private Text portNumber;
	private Composite remoteConnectionBox;
	private Text connection;
	private String savedJtagDevice;

	
	public String getName() {
		return TAB_NAME;
	}

	public Image getImage() {
		return GDBJtagImages.getDebuggerTabImage();
	}
	
	public void createControl(Composite parent) {
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		setControl(sc);

		Composite comp = new Composite(sc, SWT.NONE);
		sc.setContent(comp);
		GridLayout layout = new GridLayout();
		comp.setLayout(layout);
		
		Group group = new Group(comp, SWT.NONE);
		layout = new GridLayout();
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		group.setText(Messages.getString("GDBJtagDebuggerTab.gdbSetupGroup_Text"));
		
		createCommandControl(group);
		createCommandFactoryControl(group);
		createProtocolControl(group);
		createVerboseModeControl(group);
		
		createRemoteControl(comp);
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
		StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(
				getShell());
		if (dialog.open() == StringVariableSelectionDialog.OK) {
			text.append(dialog.getVariableExpression());
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
		label.setText(Messages.getString("GDBJtagDebuggerTab.gdbCommandLabel"));
		gd = new GridData();
		gd.horizontalSpan = 3;
		label.setLayoutData(gd);
		
		gdbCommand = new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gdbCommand.setLayoutData(gd);
		gdbCommand.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		Button button = new Button(comp, SWT.NONE);
		button.setText(Messages.getString("GDBJtagDebuggerTab.gdbCommandBrowse"));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				browseButtonSelected(Messages.getString("GDBJtagDebuggerTab.gdbCommandBrowse_Title"), gdbCommand);
			}
		});
		
		button = new Button(comp, SWT.NONE);
		button.setText(Messages.getString("GDBJtagDebuggerTab.gdbCommandVariable"));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				variablesButtonSelected(gdbCommand);
			}
		});
	}
	
	private void createCommandFactoryControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);
		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("GDBJtagDebuggerTab.commandFactoryLabel"));
		
		commandFactory = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
		
		// Get the command sets
		CommandFactoryManager cfManager = MIPlugin.getDefault().getCommandFactoryManager();
		cfDescs = cfManager.getDescriptors(IGDBJtagConstants.DEBUGGER_ID);
		for (int i = 0; i < cfDescs.length; ++i) {
			commandFactory.add(cfDescs[i].getName());
		}
		
		commandFactory.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				commandFactoryChanged();
				updateLaunchConfigurationDialog();
			}
		});
	}
	
	private void createProtocolControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);
		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("GDBJtagDebuggerTab.miProtocolLabel"));
		
		miProtocol = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
		miProtocol.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
	}
	
	private void commandFactoryChanged() {
		int currsel = miProtocol.getSelectionIndex();
		String currProt = null;
		if (currsel >= 0)
			currProt = miProtocol.getItem(currsel);
		miProtocol.removeAll();
		int cfsel = commandFactory.getSelectionIndex();
		if (cfsel >= 0) {
			String[] protocols = cfDescs[cfsel].getMIVersions();
			for (int i = 0; i < protocols.length; ++i) {
				miProtocol.add(protocols[i]);
				if (currProt != null && protocols[i].equals(currProt))
					miProtocol.select(i);
			}
		}
		if (miProtocol.getSelectionIndex() < 0 && miProtocol.getItemCount() > 0)
			miProtocol.select(0);
	}
	
	private void createVerboseModeControl(Composite parent) {
		verboseMode = new Button(parent, SWT.CHECK);
		verboseMode.setText(Messages.getString("GDBJtagDebuggerTab.verboseModeLabel"));
		verboseMode.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
	}
	
	private void createRemoteControl(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		group.setText(Messages.getString("GDBJtagDebuggerTab.remoteGroup_Text"));
		
		useRemote = new Button(group, SWT.CHECK);
		useRemote.setText(Messages.getString("GDBJtagDebuggerTab.useRemote_Text"));
		useRemote.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				useRemoteChanged();
				updateLaunchConfigurationDialog();
			}
		});
		
		Composite comp = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		comp.setLayout(layout);
		
		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("GDBJtagDebuggerTab.jtagDeviceLabel"));
		
		jtagDevice = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
		
		GDBJtagDeviceContribution[] availableDevices = GDBJtagDeviceContributionFactory.
			getInstance().getGDBJtagDeviceContribution();
		for (int i = 0; i < availableDevices.length; i++) {
			jtagDevice.add(availableDevices[i].getDeviceName());
		}
		
		jtagDevice.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateDeviceIpPort(jtagDevice.getText());
				updateLaunchConfigurationDialog();
			}
		});
		
		remoteConnectionParameters = new Composite(group, SWT.NO_TRIM | SWT.NO_FOCUS);
		remoteConnectParmsLayout = new StackLayout();
		remoteConnectionParameters.setLayout(remoteConnectParmsLayout);
				
		//
		//  Create entry fields for TCP/IP connections  
		//
		
		{
			remoteTcpipBox = new Composite(remoteConnectionParameters, SWT.NO_TRIM | SWT.NO_FOCUS);
			layout = new GridLayout();
			layout.numColumns = 2;
			remoteTcpipBox.setLayout(layout);
			remoteTcpipBox.setBackground(remoteConnectionParameters.getParent().getBackground());
			
			label = new Label(remoteTcpipBox, SWT.NONE);
			label.setText(Messages.getString("GDBJtagDebuggerTab.ipAddressLabel")); //$NON-NLS-1$
			ipAddress = new Text(remoteTcpipBox, SWT.BORDER);
			gd = new GridData();
			gd.widthHint = 125;
			ipAddress.setLayoutData(gd);
			
			label = new Label(remoteTcpipBox, SWT.NONE);
			label.setText(Messages.getString("GDBJtagDebuggerTab.portNumberLabel")); //$NON-NLS-1$
			portNumber = new Text(remoteTcpipBox, SWT.BORDER);
			gd = new GridData();
			gd.widthHint = 125;
			portNumber.setLayoutData(gd);
		}
		
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

		ipAddress.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		portNumber.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				e.doit = Character.isDigit(e.character) || Character.isISOControl(e.character);
			}
		});
		portNumber.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		connection.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});		
	}
	
	/**
	 * @param text
	 */
	@SuppressWarnings("deprecation")
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
						IGDBJtagConnection connectionDevice = (IGDBJtagConnection)selectedDevice;
						connection.setText(connectionDevice.getDefaultDeviceConnection());
					} else {
						// legacy way 
						ipAddress.setText(selectedDevice.getDefaultIpAddress());
						portNumber.setText(selectedDevice.getDefaultPortNumber());
					}
					useRemoteChanged();
					updateLaunchConfigurationDialog();
					break;
				}
			}
		}
	}

	private void useRemoteChanged() {
		boolean enabled = useRemote.getSelection();
		jtagDevice.setEnabled(enabled);
		ipAddress.setEnabled(enabled);
		portNumber.setEnabled(enabled);
		connection.setEnabled(enabled);
		GDBJtagDeviceContribution selectedDeviceEntry = findJtagDeviceByName(jtagDevice.getText());
		if ((selectedDeviceEntry == null) || (selectedDeviceEntry.getDevice() == null)) {
			remoteConnectParmsLayout.topControl = null;
			remoteConnectionParameters.layout();
		} else {
			IGDBJtagDevice device = selectedDeviceEntry.getDevice();
			if (device instanceof IGDBJtagConnection) {
				remoteConnectParmsLayout.topControl = remoteConnectionBox;
				remoteConnectionBox.getParent().layout();
			} else {
				remoteConnectParmsLayout.topControl = remoteTcpipBox;
				remoteTcpipBox.getParent().layout();
			}
		}
	}
	
	private GDBJtagDeviceContribution findJtagDeviceByName(String name) {
		GDBJtagDeviceContribution[] availableDevices = GDBJtagDeviceContributionFactory.getInstance().getGDBJtagDeviceContribution();
		for (GDBJtagDeviceContribution device : availableDevices) {
			if (device.getDeviceName().equals(name)) {
				return device;
			}
		}
		return null;
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			String gdbCommandAttr = configuration.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, IMILaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT);
			gdbCommand.setText(gdbCommandAttr);
			
			CommandFactoryManager cfManager = MIPlugin.getDefault().getCommandFactoryManager();
			CommandFactoryDescriptor defDesc = cfManager.getDefaultDescriptor(IGDBJtagConstants.DEBUGGER_ID);
			String commandFactoryAttr = configuration.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_COMMAND_FACTORY, defDesc.getName());
			int cfid = 0;
			for (int i = 0; i < cfDescs.length; ++i)
				if (cfDescs[i].getName().equals(commandFactoryAttr)) {
					cfid = i;
					break;
				}
			commandFactory.select(cfid); // populates protocol list too

			String miProtocolAttr = configuration.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_PROTOCOL, defDesc.getMIVersions()[0]);
			int n = miProtocol.getItemCount();
			for (int i = 0; i < n; ++i) {
				if (miProtocol.getItem(i).equals(miProtocolAttr)) {
					miProtocol.select(i);
				}
			}
			
			boolean verboseModeAttr = configuration.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_VERBOSE_MODE, IMILaunchConfigurationConstants.DEBUGGER_VERBOSE_MODE_DEFAULT);
			verboseMode.setSelection(verboseModeAttr);

			boolean useRemoteAttr = configuration.getAttribute(IGDBJtagConstants.ATTR_USE_REMOTE_TARGET, IGDBJtagConstants.DEFAULT_USE_REMOTE_TARGET);
			useRemote.setSelection(useRemoteAttr);
			useRemoteChanged();
			
			savedJtagDevice = configuration.getAttribute(IGDBJtagConstants.ATTR_JTAG_DEVICE, ""); //$NON-NLS-1$
			if (savedJtagDevice.length() == 0) {
				jtagDevice.select(0);
			} else {
				String storedAddress = ""; //$NON-NLS-1$
				int storedPort = 0;
				String storedConnection = ""; //$NON-NLS-1$
				
				for (int i = 0; i < jtagDevice.getItemCount(); i++) {
					if (jtagDevice.getItem(i).equals(savedJtagDevice)) {
						storedAddress = configuration.getAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS, IGDBJtagConstants.DEFAULT_IP_ADDRESS); //$NON-NLS-1$
						storedPort = configuration.getAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER, IGDBJtagConstants.DEFAULT_PORT_NUMBER);
						storedConnection = configuration.getAttribute(IGDBJtagConstants.ATTR_CONNECTION, IGDBJtagConstants.DEFAULT_CONNECTION); //$NON-NLS-1$
						jtagDevice.select(i);
						break;
					}
				}

				// New generic connection settings				
				try {
					connection.setText(new URI(storedConnection).getSchemeSpecificPart());
				} catch (URISyntaxException e) {
					Activator.log(e);
				}

				// Legacy TCP/IP based settings
				ipAddress.setText(storedAddress);
				String portString = (0<storedPort)&&(storedPort<=65535) ? Integer.valueOf(storedPort).toString() : "";  //$NON-NLS-1$
				portNumber.setText(portString);
			}
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

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, gdbCommand.getText().trim());
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME, gdbCommand.getText().trim()); // DSF
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_COMMAND_FACTORY, commandFactory.getText());
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_PROTOCOL, miProtocol.getText());
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_VERBOSE_MODE, verboseMode.getSelection());
		savedJtagDevice = jtagDevice.getText();
		configuration.setAttribute(IGDBJtagConstants.ATTR_JTAG_DEVICE, savedJtagDevice);
		configuration.setAttribute(IGDBJtagConstants.ATTR_USE_REMOTE_TARGET, useRemote.getSelection());
		if (savedJtagDevice.length() > 0) {
			try {
				IGDBJtagDevice device = findJtagDeviceByName(jtagDevice.getText()).getDevice();
				if (device instanceof IGDBJtagConnection) {
					String conn = connection.getText().trim();
					URI uri = new URI("gdb", conn, ""); //$NON-NLS-1$ //$NON-NLS-2$
					configuration.setAttribute(IGDBJtagConstants.ATTR_CONNECTION, uri.toString());
				} else {
					String ip = ipAddress.getText().trim();
					configuration.setAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS, ip);
					int port = Integer.valueOf(portNumber.getText().trim()).intValue();
					configuration.setAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER, port);
				}
			} catch (URISyntaxException e) {
				Activator.log(e);
			} catch (NumberFormatException e) {
				Activator.log(e);
			}
		}
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, IMILaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT);
		CommandFactoryManager cfManager = MIPlugin.getDefault().getCommandFactoryManager();
		CommandFactoryDescriptor defDesc = cfManager.getDefaultDescriptor(IGDBJtagConstants.DEBUGGER_ID);
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_COMMAND_FACTORY, defDesc.getName());
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_PROTOCOL, defDesc.getMIVersions()[0]);
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_VERBOSE_MODE, IMILaunchConfigurationConstants.DEBUGGER_VERBOSE_MODE_DEFAULT);
		configuration.setAttribute(IGDBJtagConstants.ATTR_USE_REMOTE_TARGET, IGDBJtagConstants.DEFAULT_USE_REMOTE_TARGET);
	}

}
