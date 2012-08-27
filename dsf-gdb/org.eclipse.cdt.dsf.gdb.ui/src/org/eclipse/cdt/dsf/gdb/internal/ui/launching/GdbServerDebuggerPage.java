/*******************************************************************************
 * Copyright (c) 2008, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     IBM Corporation
 *     Ericsson             - Modified for DSF
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import org.eclipse.cdt.debug.internal.ui.dialogfields.ComboDialogField;
import org.eclipse.cdt.debug.internal.ui.dialogfields.DialogField;
import org.eclipse.cdt.debug.internal.ui.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 * The dynamic debugger tab for remote launches using gdb server.
 */
public class GdbServerDebuggerPage extends GdbDebuggerPage {

	private final static String CONNECTION_TCP = LaunchUIMessages.getString("GDBServerDebuggerPage.0"); //$NON-NLS-1$

	private final static String CONNECTION_SERIAL = LaunchUIMessages.getString("GDBServerDebuggerPage.1"); //$NON-NLS-1$

	private ComboDialogField fConnectionField;

	private String[] fConnections = new String[]{ CONNECTION_TCP, CONNECTION_SERIAL };

	private TCPSettingsBlock fTCPBlock;

	private SerialPortSettingsBlock fSerialBlock;
	
	private Composite fConnectionStack;

	private Button fMultiButton;
	private Text fRemoteBinaryText;

	private boolean fIsInitializing = false;

	public GdbServerDebuggerPage() {
		super();
		fConnectionField = createConnectionField();
		fTCPBlock = new TCPSettingsBlock();
		fSerialBlock = new SerialPortSettingsBlock();
		fTCPBlock.addObserver(this);
		fSerialBlock.addObserver(this);
	}

	protected void createConnectionTab(TabFolder tabFolder) {
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(LaunchUIMessages.getString("GDBServerDebuggerPage.10")); //$NON-NLS-1$
		Composite comp1 = ControlFactory.createCompositeEx(tabFolder, 1, GridData.FILL_BOTH);
		((GridLayout)comp1.getLayout()).makeColumnsEqualWidth = false;
		comp1.setFont(tabFolder.getFont());
		tabItem.setControl(comp1);
		createRemoteOptions(comp1);
		Group group = new Group(comp1, SWT.NONE);
		group.setText("Connection");
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		fConnectionField.doFillIntoGrid(group, 2);
		((GridData)fConnectionField.getComboControl(null).getLayoutData()).horizontalAlignment = GridData.BEGINNING;
		fConnectionStack = ControlFactory.createCompositeEx(group, 1, GridData.FILL_BOTH);
		StackLayout stackLayout = new StackLayout();
		fConnectionStack.setLayout(stackLayout);
		((GridData)fConnectionStack.getLayoutData()).horizontalSpan = 2;
		fTCPBlock.createBlock(fConnectionStack);
		fSerialBlock.createBlock(fConnectionStack);
	}

	private ComboDialogField createConnectionField() {
		ComboDialogField field = new ComboDialogField(SWT.DROP_DOWN | SWT.READ_ONLY);
		field.setLabelText(LaunchUIMessages.getString("GDBServerDebuggerPage.9")); //$NON-NLS-1$
		field.setItems(fConnections);
		field.setDialogFieldListener(new IDialogFieldListener() {

			@Override
			public void dialogFieldChanged(DialogField f) {
				if (!isInitializing())
					connectionTypeChanged();
			}
		});
		return field;
	}

	protected void connectionTypeChanged() {
		connectionTypeChanged0();
		updateLaunchConfigurationDialog();
	}

	private void connectionTypeChanged0() {
		((StackLayout)fConnectionStack.getLayout()).topControl = null;
		int index = fConnectionField.getSelectionIndex();
		if (index >= 0 && index < fConnections.length) {
			String[] connTypes = fConnectionField.getItems();
			if (CONNECTION_TCP.equals(connTypes[index]))
				((StackLayout)fConnectionStack.getLayout()).topControl = fTCPBlock.getControl();
			else if (CONNECTION_SERIAL.equals(connTypes[index]))
				((StackLayout)fConnectionStack.getLayout()).topControl = fSerialBlock.getControl();
		}
		fConnectionStack.layout();
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		if (super.isValid(launchConfig)) {
			setErrorMessage(null);
			setMessage(null);
			
			if (fMultiButton.getSelection() && fRemoteBinaryText.getText().trim().length() == 0) {
				setErrorMessage("Remote binary path must be specified if gdbserver is started in daemon mode");
				return false;
			}

			int index = fConnectionField.getSelectionIndex();
			if (index >= 0 && index < fConnections.length) {
				String[] connTypes = fConnectionField.getItems();
				if (CONNECTION_TCP.equals(connTypes[index])) {
					if (!fTCPBlock.isValid(launchConfig)) {
						setErrorMessage(fTCPBlock.getErrorMessage());
						return false;
					}
				}
				else if (CONNECTION_SERIAL.equals(connTypes[index])) {
					if (!fSerialBlock.isValid(launchConfig)) {
						setErrorMessage(fSerialBlock.getErrorMessage());
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		setInitializing(true);
		super.initializeFrom(configuration);
		fMultiButton.setSelection(false);
		try {
			fMultiButton.setSelection(configuration.getAttribute(
					IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_MULTI, 
					IGDBLaunchConfigurationConstants.DEBUGGER_REMOTE_MULTI_DEFAULT));
			fRemoteBinaryText.setText(configuration.getAttribute(
					IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_BINARY, "")); //$NON-NLS-1$
		}
		catch( CoreException e1 ) {
		}
		boolean isTcp = true;
		try {
			isTcp = configuration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_REMOTE_TCP, true);
		}
		catch(CoreException e) {
		}
		fTCPBlock.initializeFrom(configuration);
		fSerialBlock.initializeFrom(configuration);
		fConnectionField.selectItem((isTcp) ? 0 : 1);
		remoteOptionsChanged0();
		connectionTypeChanged0();
		setInitializing(false);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_MULTI, fMultiButton.getSelection());
		// FIXME: Add UI to specify the target mode
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_EXTENDED, fMultiButton.getSelection());
		if (fMultiButton.getSelection())
			configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_BINARY, fRemoteBinaryText.getText());
		if (fConnectionField != null)
			configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_REMOTE_TCP, fConnectionField.getSelectionIndex() == 0);
		fTCPBlock.performApply(configuration);
		fSerialBlock.performApply(configuration);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		super.setDefaults(configuration);
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_REMOTE_TCP, true);
		configuration.setAttribute(
				IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_MULTI, 
				IGDBLaunchConfigurationConstants.DEBUGGER_REMOTE_MULTI_DEFAULT);
		configuration.setAttribute(
				IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_EXTENDED, 
				IGDBLaunchConfigurationConstants.DEBUGGER_REMOTE_EXTENDED_DEFAULT);
		configuration.setAttribute(
				IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_TERMINATE, 
				IGDBLaunchConfigurationConstants.DEBUGGER_REMOTE_TERMINATE_DEFAULT);
		fTCPBlock.setDefaults(configuration);
		fSerialBlock.setDefaults(configuration);
	}

	@Override
	protected boolean isInitializing() {
		return fIsInitializing;
	}

	private void setInitializing(boolean isInitializing) {
		fIsInitializing = isInitializing;
	}

	@Override
	public void createTabs(TabFolder tabFolder) {
		super.createTabs(tabFolder);
		createConnectionTab(tabFolder);
	}

	private void createRemoteOptions(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		fMultiButton = createCheckButton(group, "Daemon mode (--multi)");
		fMultiButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				remoteOptionsChanged();
			}
		});

		Composite comp = new Composite(group, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		comp.setLayout(layout);
		comp.setLayoutData(gd);
		new Label(comp, SWT.NONE).setText("Binary path on target: ");
		fRemoteBinaryText = new Text(comp, SWT.BORDER | SWT.SINGLE);
		fRemoteBinaryText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fRemoteBinaryText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText( ModifyEvent e ) {
				remoteOptionsChanged();
			}
		});
	}

	private void remoteOptionsChanged0() {
		fRemoteBinaryText.setEnabled(fMultiButton.getSelection());
	}

	private void remoteOptionsChanged() {
		remoteOptionsChanged0();
		updateLaunchConfigurationDialog();
	}
}
