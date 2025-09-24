/*******************************************************************************
 * Copyright (c) 2000, 2025 QNX Software Systems and others.
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
 *     Intel Corporation - Update for Core Build (#1222)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import java.io.IOException;

import org.eclipse.cdt.debug.internal.ui.dialogfields.ComboDialogField;
import org.eclipse.cdt.debug.internal.ui.dialogfields.DialogField;
import org.eclipse.cdt.debug.internal.ui.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.cdt.serial.StandardBaudRates;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class SerialPortSettingsBlock extends AbstractSettingsBlock {

	private ComboDialogField fDeviceField;

	private ComboDialogField fSpeedField;

	private String[] fSerialPorts;

	public SerialPortSettingsBlock() {
		super();
		try {
			fSerialPorts = SerialPort.list();

		} catch (IOException e) {
			GdbUIPlugin.log(e);
			fSerialPorts = new String[0];
		}
		fDeviceField = createDeviceField();
		fSpeedField = createSpeedField();
	}

	@Override
	public void createBlock(Composite parent) {
		fShell = parent.getShell();
		Composite comp = ControlFactory.createCompositeEx(parent, 2, GridData.FILL_BOTH);
		((GridLayout) comp.getLayout()).makeColumnsEqualWidth = false;
		((GridLayout) comp.getLayout()).marginHeight = 0;
		((GridLayout) comp.getLayout()).marginWidth = 0;
		comp.setFont(parent.getFont());
		fDeviceField.doFillIntoGrid(comp, 2);
		((GridData) fDeviceField.getComboControl(null).getLayoutData()).horizontalAlignment = GridData.BEGINNING;
		fSpeedField.doFillIntoGrid(comp, 2);
		((GridData) fSpeedField.getComboControl(null).getLayoutData()).horizontalAlignment = GridData.BEGINNING;
		setControl(comp);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		initializeDevice(configuration);
		initializeSpeed(configuration);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV,
				fSerialPorts.length > 0 ? fSerialPorts[0] : ""); //$NON-NLS-1$
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV_SPEED,
				"" + StandardBaudRates.getDefault()); //$NON-NLS-1$
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (fDeviceField != null)
			configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV, fDeviceField.getText().trim());
		if (fSpeedField != null) {
			configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV_SPEED, fSpeedField.getText().trim());
		}
	}

	private ComboDialogField createDeviceField() {
		// The user is allowed to enter a custom device name.
		ComboDialogField field = new ComboDialogField(SWT.DROP_DOWN);
		field.setLabelText(LaunchUIMessages.getString("SerialPortSettingsBlock.0")); //$NON-NLS-1$
		field.setItems(fSerialPorts);
		field.setDialogFieldListener(new IDialogFieldListener() {

			@Override
			public void dialogFieldChanged(DialogField f) {
				deviceFieldChanged();
			}
		});
		return field;
	}

	private ComboDialogField createSpeedField() {
		// The user is allowed to enter a custom speed.
		ComboDialogField field = new ComboDialogField(SWT.DROP_DOWN);
		field.setLabelText(LaunchUIMessages.getString("SerialPortSettingsBlock.1")); //$NON-NLS-1$
		field.setItems(StandardBaudRates.asStringArray());
		field.setDialogFieldListener(new IDialogFieldListener() {

			@Override
			public void dialogFieldChanged(DialogField f) {
				speedFieldChanged();
			}
		});
		return field;
	}

	protected void deviceFieldChanged() {
		updateErrorMessage();
		setChanged();
		notifyObservers();
	}

	protected void speedFieldChanged() {
		updateErrorMessage();
		setChanged();
		notifyObservers();
	}

	private void initializeDevice(ILaunchConfiguration configuration) {
		if (fDeviceField != null) {
			try {
				String defaultPort = fSerialPorts.length > 0 ? fSerialPorts[0] : ""; //$NON-NLS-1$
				String port = configuration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV, defaultPort);
				fDeviceField.setText(port);
				initializeField(configuration, fDeviceField, (Composite) fControl);
			} catch (CoreException e) {
			}
		}
	}

	private void initializeSpeed(ILaunchConfiguration configuration) {
		if (fSpeedField != null) {
			try {
				String rate = configuration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV_SPEED,
						"" + StandardBaudRates.getDefault()); //$NON-NLS-1$
				fSpeedField.setText(rate);
				initializeField(configuration, fSpeedField, (Composite) fControl);
			} catch (CoreException e) {
			}
		}
	}

	@Override
	protected void updateErrorMessage() {
		setErrorMessage(null);
		if (fDeviceField != null && fSpeedField != null) {
			if (fDeviceField.getText().isBlank())
				setErrorMessage(LaunchUIMessages.getString("SerialPortSettingsBlock.2")); //$NON-NLS-1$
			else if (!deviceIsValid(fDeviceField.getText().trim()))
				setErrorMessage(LaunchUIMessages.getString("SerialPortSettingsBlock.3")); //$NON-NLS-1$
			else if (fSpeedField.getText().isBlank())
				setErrorMessage(LaunchUIMessages.getString("SerialPortSettingsBlock.4")); //$NON-NLS-1$
		}
	}

	private boolean deviceIsValid(String serialPort) {
		return true; // The user may enter a custom device name.
	}
}
