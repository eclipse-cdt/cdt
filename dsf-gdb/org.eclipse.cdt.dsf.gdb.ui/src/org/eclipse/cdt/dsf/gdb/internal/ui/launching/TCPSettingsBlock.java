/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
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
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import java.util.Observable;

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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class TCPSettingsBlock extends Observable {

	private final static String DEFAULT_HOST_NAME = "localhost"; //$NON-NLS-1$

	private final static String DEFAULT_PORT_NUMBER = "10000"; //$NON-NLS-1$

	private Shell fShell;

	private StringDialogField fHostNameField;

	private StringDialogField fPortNumberField;

	private Control fControl;

	private String fErrorMessage = null;

	public TCPSettingsBlock() {
		super();
		fHostNameField = createHostNameField();
		fPortNumberField = createPortNumberField();
	}

	public void createBlock(Composite parent) {
		fShell = parent.getShell();
		Composite comp = ControlFactory.createCompositeEx(parent, 2, GridData.FILL_BOTH);
		((GridLayout) comp.getLayout()).makeColumnsEqualWidth = false;
		((GridLayout) comp.getLayout()).marginHeight = 0;
		((GridLayout) comp.getLayout()).marginWidth = 0;
		comp.setFont(parent.getFont());
		PixelConverter converter = new PixelConverter(comp);
		fHostNameField.doFillIntoGrid(comp, 2);
		LayoutUtil.setWidthHint(fHostNameField.getTextControl(null), converter.convertWidthInCharsToPixels(20));
		fPortNumberField.doFillIntoGrid(comp, 2);
		((GridData) fPortNumberField.getTextControl(null).getLayoutData()).horizontalAlignment = GridData.BEGINNING;
		LayoutUtil.setWidthHint(fPortNumberField.getTextControl(null), converter.convertWidthInCharsToPixels(10));
		setControl(comp);
	}

	protected Shell getShell() {
		return fShell;
	}

	public void dispose() {
		deleteObservers();
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		initializeHostName(configuration);
		initializePortNumber(configuration);
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_HOST, DEFAULT_HOST_NAME);
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_PORT, DEFAULT_PORT_NUMBER);
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (fHostNameField != null)
			configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_HOST, fHostNameField.getText().trim());
		if (fPortNumberField != null)
			configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_PORT, fPortNumberField.getText().trim());
	}

	private StringDialogField createHostNameField() {
		StringDialogField field = new StringDialogField();
		field.setLabelText(LaunchUIMessages.getString("TCPSettingsBlock.0")); //$NON-NLS-1$
		field.setDialogFieldListener(new IDialogFieldListener() {

			@Override
			public void dialogFieldChanged(DialogField f) {
				hostNameFieldChanged();
			}
		});
		return field;
	}

	private StringDialogField createPortNumberField() {
		StringDialogField field = new StringDialogField();
		field.setLabelText(LaunchUIMessages.getString("TCPSettingsBlock.1")); //$NON-NLS-1$
		field.setDialogFieldListener(new IDialogFieldListener() {

			@Override
			public void dialogFieldChanged(DialogField f) {
				portNumberFieldChanged();
			}
		});
		return field;
	}

	protected void hostNameFieldChanged() {
		updateErrorMessage();
		setChanged();
		notifyObservers();
	}

	protected void portNumberFieldChanged() {
		updateErrorMessage();
		setChanged();
		notifyObservers();
	}

	private void initializeHostName(ILaunchConfiguration configuration) {
		if (fHostNameField != null) {
			try {
				fHostNameField.setText(
						configuration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_HOST, DEFAULT_HOST_NAME));
			} catch (CoreException e) {
			}
		}
	}

	private void initializePortNumber(ILaunchConfiguration configuration) {
		if (fPortNumberField != null) {
			try {
				fPortNumberField.setText(
						configuration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_PORT, DEFAULT_PORT_NUMBER));
			} catch (CoreException e) {
			}
		}
	}

	public Control getControl() {
		return fControl;
	}

	protected void setControl(Control control) {
		fControl = control;
	}

	public boolean isValid(ILaunchConfiguration configuration) {
		updateErrorMessage();
		return (getErrorMessage() == null);
	}

	private void updateErrorMessage() {
		setErrorMessage(null);
		if (fHostNameField != null && fPortNumberField != null) {
			if (fHostNameField.getText().trim().length() == 0)
				setErrorMessage(LaunchUIMessages.getString("TCPSettingsBlock.2")); //$NON-NLS-1$
			else if (!hostNameIsValid(fHostNameField.getText().trim()))
				setErrorMessage(LaunchUIMessages.getString("TCPSettingsBlock.3")); //$NON-NLS-1$
			else if (fPortNumberField.getText().trim().length() == 0)
				setErrorMessage(LaunchUIMessages.getString("TCPSettingsBlock.4")); //$NON-NLS-1$
			else if (!portNumberIsValid(fPortNumberField.getText().trim()))
				setErrorMessage(LaunchUIMessages.getString("TCPSettingsBlock.5")); //$NON-NLS-1$
		}
	}

	public String getErrorMessage() {
		return fErrorMessage;
	}

	private void setErrorMessage(String string) {
		fErrorMessage = string;
	}

	private boolean hostNameIsValid(String hostName) {
		return true;
	}

	private boolean portNumberIsValid(String portNumber) {
		try {
			int port = Integer.parseInt(portNumber);
			return (port > 0 && port <= 0xFFFF);
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
