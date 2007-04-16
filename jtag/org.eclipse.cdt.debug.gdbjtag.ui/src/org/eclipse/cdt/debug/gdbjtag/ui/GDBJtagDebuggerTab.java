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
import org.eclipse.cdt.debug.mi.core.MIPlugin;
import org.eclipse.cdt.debug.mi.core.command.factories.CommandFactoryDescriptor;
import org.eclipse.cdt.debug.mi.core.command.factories.CommandFactoryManager;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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
import org.eclipse.swt.widgets.Text;

/**
 * @author Doug Schaefer
 *
 */
public class GDBJtagDebuggerTab extends AbstractLaunchConfigurationTab {

	private CommandFactoryDescriptor[] cfDescs;
	private int cfSelected = -1;
	
	private Button useRemote;
	private Composite remoteConnection;
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
		
		Text text = new Text(comp, SWT.SINGLE | SWT.BORDER);
		text.setText("gdb");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		text.setLayoutData(gd);

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
		
		Text text = new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		text.setLayoutData(gd);

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
		
		Combo combo = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
		
		// Get the command sets
		CommandFactoryManager cfManager = MIPlugin.getDefault().getCommandFactoryManager();
		CommandFactoryDescriptor defDesc = cfManager.getDefaultDescriptor(GDBJtagConstants.DEBUGGER_ID);
		cfDescs = cfManager.getDescriptors(
				GDBJtagConstants.DEBUGGER_ID);
		for (int i = 0; i < cfDescs.length; ++i) {
			combo.add(cfDescs[i].getName());
			if (defDesc == cfDescs[i])
				cfSelected = i;
		}
	
		if (cfSelected > -1)
			combo.select(cfSelected);
	}
	
	public void createProtocolControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);
		Label label = new Label(comp, SWT.NONE);
		label.setText("Protocol Version:");
		
		Combo combo = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
		if (cfSelected > -1) {
			String[] vers = cfDescs[cfSelected].getMIVersions();
			for (int i = 0; i < vers.length; ++i) {
				combo.add(vers[i]);
			}
		}
		combo.select(0);
	}
	
	public void createVerboseModeControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);
		
		Button button = new Button(comp, SWT.CHECK);
		Label label = new Label(comp, SWT.NONE);
		label.setText("Verbose console mode");
	}
	
	private void createRemoteControl(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		group.setText("Remote Connection");
		
		useRemote = new Button(group, SWT.CHECK);
		useRemote.setText("Use remote connection");
		useRemote.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				useRemoteChanged();
			}
		});
		
		remoteConnection = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		remoteConnection.setLayout(layout);
		
		Label label = new Label(remoteConnection, SWT.NONE);
		label.setText("Host name or IP address:");
		ipAddress = new Text(remoteConnection, SWT.BORDER);
		
		label = new Label(remoteConnection, SWT.NONE);
		label.setText("Port number:");
		portNumber = new Text(remoteConnection, SWT.BORDER);
	}
	
	private void useRemoteChanged() {
		boolean enabled = useRemote.getSelection();
		ipAddress.setEnabled(enabled);
		portNumber.setEnabled(enabled);
	}
	
	public void initializeFrom(ILaunchConfiguration configuration) {
		useRemote.setSelection(true);
		useRemoteChanged();
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		// TODO Auto-generated method stub
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// TODO Auto-generated method stub
	}

}
