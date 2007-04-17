/**********************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.debug.gdbjtag.ui;

import org.eclipse.cdt.debug.gdbjtag.core.GDBJtagConstants;
import org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants;
import org.eclipse.cdt.debug.mi.core.MIPlugin;
import org.eclipse.cdt.debug.mi.core.command.factories.CommandFactoryDescriptor;
import org.eclipse.cdt.debug.mi.core.command.factories.CommandFactoryManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Doug Schaefer
 *
 */
public class GDBJtagDebuggerTab extends AbstractLaunchConfigurationTab {

	private CommandFactoryDescriptor[] cfDescs;
	
	private Text gdbCommand;
	private Text gdbinitFile;
	private Combo commandFactory;
	private Combo miProtocol;
	private Button verboseMode;
	
	private Button useRemote;
	private Composite remoteTarget;
	private Text ipAddress;
	private Text portNumber;
	
	public String getName() {
		return "Debugger";
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
		group.setText("GDB Setup");
		
		createCommandControl(group);
		createInitFileControl(group);
		createCommandSetControl(group);
		createProtocolControl(group);
		createVerboseModeControl(group);
		
		createRemoteControl(comp);
	}

	public void createCommandControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		comp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		comp.setLayoutData(gd);
		
		Label label = new Label(comp, SWT.NONE);
		label.setText("GDB Command:");
		gd = new GridData();
		gd.horizontalSpan = 3;
		label.setLayoutData(gd);
		
		gdbCommand = new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gdbCommand.setLayoutData(gd);

		Button button = new Button(comp, SWT.NONE);
		button.setText("Browse...");
		button = new Button(comp, SWT.NONE);
		button.setText("Variables...");
	}
	
	public void createInitFileControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		comp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		comp.setLayoutData(gd);
		
		Label label = new Label(comp, SWT.NONE);
		label.setText("GDB Init File:");
		gd = new GridData();
		gd.horizontalSpan = 3;
		label.setLayoutData(gd);
		
		gdbinitFile = new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gdbinitFile.setLayoutData(gd);

		Button button = new Button(comp, SWT.NONE);
		button.setText("Browse...");
		button = new Button(comp, SWT.NONE);
		button.setText("Workspace...");
	}
	
	public void createCommandSetControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);
		Label label = new Label(comp, SWT.NONE);
		label.setText("Command Set:");
		
		commandFactory = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
		
		// Get the command sets
		CommandFactoryManager cfManager = MIPlugin.getDefault().getCommandFactoryManager();
		cfDescs = cfManager.getDescriptors(GDBJtagConstants.DEBUGGER_ID);
		for (int i = 0; i < cfDescs.length; ++i) {
			commandFactory.add(cfDescs[i].getName());
		}
	}
	
	public void createProtocolControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);
		Label label = new Label(comp, SWT.NONE);
		label.setText("Protocol Version:");
		
		miProtocol = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
	}
	
	private void commandSetChanged() {
		int currsel = miProtocol.getSelectionIndex();
		String currProt = miProtocol.getItem(currsel);
		miProtocol.removeAll();
		int cfsel = commandFactory.getSelectionIndex();
		if (cfsel >= 0) {
			String[] protocols = cfDescs[cfsel].getMIVersions();
			for (int i = 0; i < protocols.length; ++i) {
				miProtocol.add(protocols[i]);
				if (protocols[i].equals(currProt))
					miProtocol.select(i);
			}
		}
	}
	
	public void createVerboseModeControl(Composite parent) {
		verboseMode = new Button(parent, SWT.CHECK);
		verboseMode.setText("Verbose console mode");
	}
	
	private void createRemoteControl(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		group.setText("Remote Target");
		
		useRemote = new Button(group, SWT.CHECK);
		useRemote.setText("Use remote target");
		useRemote.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				useRemoteChanged();
			}
		});
		
		remoteTarget = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		remoteTarget.setLayout(layout);
		
		Label label = new Label(remoteTarget, SWT.NONE);
		label.setText("Host name or IP address:");
		ipAddress = new Text(remoteTarget, SWT.BORDER);
		gd = new GridData();
		gd.widthHint = 100;
		ipAddress.setLayoutData(gd);
		
		label = new Label(remoteTarget, SWT.NONE);
		label.setText("Port number:");
		portNumber = new Text(remoteTarget, SWT.BORDER);
		portNumber.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				e.doit = Character.isDigit(e.character) || Character.isISOControl(e.character);
			}
		});
		gd = new GridData();
		gd.widthHint = 100;
		portNumber.setLayoutData(gd);
	}
	
	private void useRemoteChanged() {
		boolean enabled = useRemote.getSelection();
		ipAddress.setEnabled(enabled);
		portNumber.setEnabled(enabled);
	}
	
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			gdbCommand.setText(configuration.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, IMILaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT));
			gdbinitFile.setText(configuration.getAttribute(IMILaunchConfigurationConstants.ATTR_GDB_INIT, IMILaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT));
			
			CommandFactoryManager cfManager = MIPlugin.getDefault().getCommandFactoryManager();
			CommandFactoryDescriptor defDesc = cfManager.getDefaultDescriptor(GDBJtagConstants.DEBUGGER_ID);
			String cfname = configuration.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_COMMAND_FACTORY, defDesc.getName());
			int cfid = 0;
			for (int i = 0; i < cfDescs.length; ++i)
				if (cfDescs[i].getName().equals(cfname)) {
					cfid = i;
					break;
				}
			commandFactory.select(cfid);

			String protname = configuration.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_PROTOCOL, defDesc.getMIVersions()[0]);
			miProtocol.removeAll();
			String[] protocols = cfDescs[cfid].getMIVersions();
			for (int i = 0; i < protocols.length; ++i) {
				miProtocol.add(protocols[i]);
				if (protocols[i].equals(protname))
					miProtocol.select(i);
			}

			verboseMode.setSelection(configuration.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_VERBOSE_MODE, IMILaunchConfigurationConstants.DEBUGGER_VERBOSE_MODE_DEFAULT));

			useRemote.setSelection(configuration.getAttribute(GDBJtagConstants.ATTR_USE_REMOTE_TARGET, GDBJtagConstants.DEFAULT_USE_REMOTE_TARGET));
			ipAddress.setText(configuration.getAttribute(GDBJtagConstants.ATTR_IP_ADDRESS, GDBJtagConstants.DEFAULT_IP_ADDRESS));
			portNumber.setText(String.valueOf(configuration.getAttribute(GDBJtagConstants.ATTR_PORT_NUMBER, GDBJtagConstants.DEFAULT_PORT_NUMBER)));
			useRemoteChanged();
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
		}
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, IMILaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT);
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_GDB_INIT, IMILaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT);
		
		CommandFactoryManager cfManager = MIPlugin.getDefault().getCommandFactoryManager();
		CommandFactoryDescriptor defDesc = cfManager.getDefaultDescriptor(GDBJtagConstants.DEBUGGER_ID);
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_COMMAND_FACTORY, defDesc.getName());
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_PROTOCOL, defDesc.getMIVersions()[0]);
		
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_VERBOSE_MODE, IMILaunchConfigurationConstants.DEBUGGER_VERBOSE_MODE_DEFAULT);
		
		configuration.setAttribute(GDBJtagConstants.ATTR_USE_REMOTE_TARGET, GDBJtagConstants.DEFAULT_USE_REMOTE_TARGET);
		configuration.setAttribute(GDBJtagConstants.ATTR_IP_ADDRESS, GDBJtagConstants.DEFAULT_IP_ADDRESS);
		configuration.setAttribute(GDBJtagConstants.ATTR_PORT_NUMBER, GDBJtagConstants.DEFAULT_PORT_NUMBER);
	}

}
