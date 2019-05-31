/*******************************************************************************
 * Copyright (c) 2019 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GDBRemoteSerialLaunchTargetProvider;
import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetWorkingCopy;
import org.eclipse.launchbar.ui.target.LaunchTargetWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewGdbRemoteSerialTargetWizard extends LaunchTargetWizard {

	private Button sameAsPortname;
	private Text nameText;
	private Combo portCombo;
	private Text baudText;

	private class SerialPage extends WizardPage {
		public SerialPage() {
			super(NewGdbRemoteTCPTargetWizard.class.getName());
			setTitle(LaunchUIMessages.getString("NewGDBRemoteSerialTargetWizard_Title")); //$NON-NLS-1$
			setDescription(LaunchUIMessages.getString("NewGDBRemoteSerialTargetWizard_Desc")); //$NON-NLS-1$
		}

		@Override
		public void createControl(Composite parent) {
			Composite control = new Composite(parent, SWT.NONE);
			control.setLayout(new GridLayout());

			// Target name

			Group nameGroup = new Group(control, SWT.NONE);
			nameGroup.setText(LaunchUIMessages.getString("NewGDBRemoteSerialTargetWizard_NameGroup")); //$NON-NLS-1$
			nameGroup.setLayout(new GridLayout(2, false));
			nameGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			sameAsPortname = new Button(nameGroup, SWT.CHECK);
			sameAsPortname.setText(LaunchUIMessages.getString("NewGDBRemoteSerialTargetWizard_SameAsSerialPort")); //$NON-NLS-1$
			GridData gridData = new GridData();
			gridData.horizontalSpan = 2;
			sameAsPortname.setLayoutData(gridData);
			sameAsPortname.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					boolean same = sameAsPortname.getSelection();
					if (same) {
						nameText.setText(portCombo.getText());
					}
					nameText.setEnabled(!same);
				}
			});
			sameAsPortname.setSelection(true);

			Label nameLabel = new Label(nameGroup, SWT.NONE);
			nameLabel.setText(LaunchUIMessages.getString("NewGDBRemoteSerialTargetWizard_TargetName")); //$NON-NLS-1$

			nameText = new Text(nameGroup, SWT.BORDER);
			nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			nameText.setEnabled(false);
			nameText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					validatePage();
				}
			});

			// serial port

			Group connGroup = new Group(control, SWT.NONE);
			connGroup.setText(LaunchUIMessages.getString("NewGDBRemoteSerialTargetWizard_ConnectionGroup")); //$NON-NLS-1$
			connGroup.setLayout(new GridLayout(2, false));
			connGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			Label portLabel = new Label(connGroup, SWT.NONE);
			portLabel.setText(LaunchUIMessages.getString("NewGDBRemoteSerialTargetWizard_SerialPort")); //$NON-NLS-1$

			portCombo = new Combo(connGroup, SWT.NONE);
			portCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			try {
				String[] portNames = SerialPort.list();
				for (String portName : portNames) {
					portCombo.add(portName);
				}
				if (portNames.length > 0) {
					portCombo.select(0);
					nameText.setText(portCombo.getText());
				}
			} catch (IOException e) {
				GdbUIPlugin.log(e);
			}

			portCombo.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					if (sameAsPortname.getSelection()) {
						nameText.setText(portCombo.getText());
					}
					validatePage();
				}
			});

			Label baudLabel = new Label(connGroup, SWT.NONE);
			baudLabel.setText(LaunchUIMessages.getString("NewGDBRemoteSerialTargetWizard_BaudRate")); //$NON-NLS-1$

			baudText = new Text(connGroup, SWT.BORDER);
			baudText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			baudText.setText("115200"); //$NON-NLS-1$
			baudText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					validatePage();
				}
			});

			setControl(control);
			validatePage();
		}

		private void validatePage() {
			setPageComplete(false);

			String port = portCombo.getText();
			if (port.isEmpty()) {
				setErrorMessage(LaunchUIMessages.getString("NewGDBRemoteSerialTargetWizard_NoSerialPort")); //$NON-NLS-1$
				return;
			}

			try {
				String[] ports = SerialPort.list();
				Arrays.sort(ports);
				if (Arrays.binarySearch(ports, port) < 0) {
					setMessage("Serial port not found on this system", WARNING); //$NON-NLS-1$
				} else {
					setMessage(null, WARNING);
				}
			} catch (IOException e) {
				setErrorMessage(e.getLocalizedMessage());
				return;
			}
			String baud = baudText.getText();
			if (baud.isEmpty()) {
				setErrorMessage(LaunchUIMessages.getString("NewGDBRemoteSerialTargetWizard_NoBaudRate")); //$NON-NLS-1$
				return;
			}

			try {
				Integer.parseInt(baud);
			} catch (NumberFormatException e) {
				setErrorMessage(LaunchUIMessages.getString("NewGDBRemoteSerialTargetWizard_BaudNotANumber")); //$NON-NLS-1$
				return;
			}

			if (nameText.getText().isEmpty()) {
				setErrorMessage(LaunchUIMessages.getString("NewGDBRemoteSerialTargetWizard_NoTargetName")); //$NON-NLS-1$
				return;
			}

			setErrorMessage(null);
			setPageComplete(true);
		}
	}

	@Override
	public void addPages() {
		super.addPages();
		addPage(new SerialPage());
	}

	@Override
	public boolean performFinish() {
		ILaunchTargetManager manager = GdbUIPlugin.getService(ILaunchTargetManager.class);
		String id = nameText.getText();

		ILaunchTarget target = getLaunchTarget();
		if (target == null) {
			target = manager.addLaunchTarget(GDBRemoteSerialLaunchTargetProvider.TYPE_ID, id);
		}

		ILaunchTargetWorkingCopy wc = target.getWorkingCopy();
		wc.setId(id);
		wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV, portCombo.getText());
		wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV_SPEED, baudText.getText());
		wc.save();

		return true;
	}

}
