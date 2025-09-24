/*******************************************************************************
 * Copyright (c) 2019, 2025 QNX Software Systems and others.
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
import java.util.List;

import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GDBRemoteSerialLaunchTargetProvider;
import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.cdt.serial.StandardBaudRates;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetWorkingCopy;
import org.eclipse.launchbar.core.target.LaunchTargetUtils;
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
	private Combo baudCombo;
	private static List<String> existingLaunchTargetNames = LaunchTargetUtils.getExistingLaunchTargetNames();
	private String originalName = ""; //$NON-NLS-1$

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

			String targetName = ""; //$NON-NLS-1$
			String serialPort = ""; //$NON-NLS-1$
			String[] portNames;
			String baudRate = String.valueOf(StandardBaudRates.getDefault());
			ILaunchTarget launchTarget = getLaunchTarget();
			try {
				portNames = SerialPort.list();
			} catch (IOException e) {
				GdbUIPlugin.log(e);
				portNames = new String[0];
			}
			// When it's a new launch target, we start with empty strings.
			// Choosing the first port found as a default name has a big chance of a duplicate name error.
			if (launchTarget != null) {
				targetName = launchTarget.getId();
				originalName = targetName;
				serialPort = launchTarget.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV, serialPort);
				baudRate = launchTarget.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV_SPEED, baudRate);
			}

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
			sameAsPortname.setSelection(targetName.equals(serialPort));

			Label nameLabel = new Label(nameGroup, SWT.NONE);
			nameLabel.setText(LaunchUIMessages.getString("NewGDBRemoteSerialTargetWizard_TargetName")); //$NON-NLS-1$

			nameText = new Text(nameGroup, SWT.BORDER);
			nameText.setText(targetName);
			nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			nameText.setEnabled(!targetName.equals(serialPort));
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

			portCombo = new Combo(connGroup, SWT.DROP_DOWN);
			portCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			portCombo.setItems(portNames);
			portCombo.setText(serialPort);

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

			baudCombo = new Combo(connGroup, SWT.DROP_DOWN);
			baudCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			baudCombo.setItems(StandardBaudRates.asStringArray());
			baudCombo.setText(baudRate);

			baudCombo.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					validatePage();
				}
			});

			setControl(control);
		}

		private void validatePage() {
			setPageComplete(false);

			String port = portCombo.getText();
			if (port.isBlank()) {
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
			String baud = baudCombo.getText();
			if (baud.isBlank()) {
				setErrorMessage(LaunchUIMessages.getString("NewGDBRemoteSerialTargetWizard_NoBaudRate")); //$NON-NLS-1$
				return;
			}

			try {
				Integer.parseInt(baud);
			} catch (NumberFormatException e) {
				setErrorMessage(LaunchUIMessages.getString("NewGDBRemoteSerialTargetWizard_BaudNotANumber")); //$NON-NLS-1$
				return;
			}

			if (nameText.getText().isBlank()) {
				setErrorMessage(LaunchUIMessages.getString("NewGDBRemoteSerialTargetWizard_NoTargetName")); //$NON-NLS-1$
				return;
			}

			if (!originalName.equals(nameText.getText().trim())
					&& existingLaunchTargetNames.contains(nameText.getText().trim())) {
				setErrorMessage(LaunchUIMessages.getString("NewGdbRemoteSerialTargetWizard_DuplicateName")); //$NON-NLS-1$
				return;
			}

			setErrorMessage(null);
			setPageComplete(true);
		}

		@Override
		public boolean isPageComplete() {
			// Disable Finish button at start, when fields are empty.
			return portCombo != null && !portCombo.getText().isBlank() && baudCombo != null
					&& !baudCombo.getText().isBlank() && nameText != null && !nameText.getText().isBlank()
					&& getErrorMessage() == null;
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
		wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV_SPEED, baudCombo.getText());
		wc.save();

		return true;
	}

	@Override
	public boolean canDelete() {
		return true;
	}

	@Override
	public void performDelete() {
		ILaunchTargetManager manager = GdbUIPlugin.getService(ILaunchTargetManager.class);
		ILaunchTarget target = getLaunchTarget();
		if (target != null) {
			manager.removeLaunchTarget(target);
		}
	}
}
