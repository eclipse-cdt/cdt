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

import org.eclipse.cdt.debug.internal.ui.dialogfields.ComboDialogField;
import org.eclipse.cdt.debug.internal.ui.dialogfields.DialogField;
import org.eclipse.cdt.debug.internal.ui.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.debug.internal.ui.dialogfields.LayoutUtil;
import org.eclipse.cdt.debug.internal.ui.dialogfields.StringDialogField;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class SerialPortSettingsBlock extends AbstractSettingsBlock {

	private final static String DEFAULT_ASYNC_DEVICE = "/dev/ttyS0"; //$NON-NLS-1$

	private final static String DEFAULT_ASYNC_DEVICE_SPEED = "115200"; //$NON-NLS-1$

	private StringDialogField fDeviceField;

	private ComboDialogField fSpeedField;

	private String fSpeedChoices[] = { "9600", "19200", "38400", "57600", "115200", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"230400", "460800", "921600", "1000000", "1152000", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"1500000", "2000000", "2500000", "3000000", "3500000", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"4000000" //$NON-NLS-1$
	};

	public SerialPortSettingsBlock() {
		super();
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
		PixelConverter converter = new PixelConverter(comp);
		fDeviceField.doFillIntoGrid(comp, 2);
		LayoutUtil.setWidthHint(fDeviceField.getTextControl(null), converter.convertWidthInCharsToPixels(20));
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
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV, DEFAULT_ASYNC_DEVICE);
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV_SPEED, DEFAULT_ASYNC_DEVICE_SPEED);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (fDeviceField != null)
			configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV, fDeviceField.getText().trim());
		if (fSpeedField != null) {
			int index = fSpeedField.getSelectionIndex();
			configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV_SPEED, getSpeedItem(index));
		}
	}

	private StringDialogField createDeviceField() {
		StringDialogField field = new StringDialogField();
		field.setLabelText(LaunchUIMessages.getString("SerialPortSettingsBlock.0")); //$NON-NLS-1$
		field.setDialogFieldListener(new IDialogFieldListener() {

			@Override
			public void dialogFieldChanged(DialogField f) {
				deviceFieldChanged();
			}
		});
		return field;
	}

	private ComboDialogField createSpeedField() {
		ComboDialogField field = new ComboDialogField(SWT.DROP_DOWN | SWT.READ_ONLY);
		field.setLabelText(LaunchUIMessages.getString("SerialPortSettingsBlock.1")); //$NON-NLS-1$
		field.setItems(fSpeedChoices);
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
				fDeviceField.setText(
						configuration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV, DEFAULT_ASYNC_DEVICE));
				initializeField(configuration, fDeviceField, (Composite) fControl);
			} catch (CoreException e) {
			}
		}
	}

	private void initializeSpeed(ILaunchConfiguration configuration) {
		if (fSpeedField != null) {
			int index = 0;
			try {
				index = getSpeedItemIndex(configuration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV_SPEED,
						DEFAULT_ASYNC_DEVICE_SPEED));
				initializeField(configuration, fSpeedField, (Composite) fControl);
			} catch (CoreException e) {
			}
			fSpeedField.selectItem(index);
		}
	}

	private String getSpeedItem(int index) {
		return (index >= 0 && index < fSpeedChoices.length) ? fSpeedChoices[index] : null;
	}

	private int getSpeedItemIndex(String item) {
		for (int i = 0; i < fSpeedChoices.length; ++i)
			if (fSpeedChoices[i].equals(item))
				return i;
		return 0;
	}

	@Override
	protected void updateErrorMessage() {
		setErrorMessage(null);
		if (fDeviceField != null && fSpeedField != null) {
			if (fDeviceField.getText().trim().length() == 0)
				setErrorMessage(LaunchUIMessages.getString("SerialPortSettingsBlock.2")); //$NON-NLS-1$
			else if (!deviceIsValid(fDeviceField.getText().trim()))
				setErrorMessage(LaunchUIMessages.getString("SerialPortSettingsBlock.3")); //$NON-NLS-1$
			else if (fSpeedField.getSelectionIndex() < 0)
				setErrorMessage(LaunchUIMessages.getString("SerialPortSettingsBlock.4")); //$NON-NLS-1$
		}
	}

	private boolean deviceIsValid(String hostName) {
		return true;
	}
}
