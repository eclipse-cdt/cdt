/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - initial contribution
 *******************************************************************************/
package org.eclipse.remote.serial.ui;

import java.util.Set;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.serial.core.ISerialPortService;
import org.eclipse.remote.serial.internal.ui.Activator;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.swt.widgets.Shell;

public class NewSerialPortConnectionWizard extends Wizard implements IRemoteUIConnectionWizard {

	private NewSerialPortConnectionWizardPage page;
	private IRemoteConnectionWorkingCopy workingCopy;
	private Shell shell;
	private IRemoteConnectionType connectionType;

	public NewSerialPortConnectionWizard(Shell shell, IRemoteConnectionType connectionType) {
		this.shell = shell;
		this.connectionType = connectionType;
	}

	@Override
	public void addPages() {
		page = new NewSerialPortConnectionWizardPage();
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		if (getConnection() == null) {
			return false;
		}

		workingCopy.setAttribute(ISerialPortService.PORT_NAME_ATTR, page.getPortName());
		workingCopy.setAttribute(ISerialPortService.BAUD_RATE_ATTR, Integer.toString(page.getBaudRateIndex()));
		workingCopy.setAttribute(ISerialPortService.BYTE_SIZE_ATTR, Integer.toString(page.getByteSizeIndex()));
		workingCopy.setAttribute(ISerialPortService.PARITY_ATTR, Integer.toString(page.getParityIndex()));
		workingCopy.setAttribute(ISerialPortService.STOP_BITS_ATTR, Integer.toString(page.getStopBitsIndex()));

		return true;
	}

	@Override
	public IRemoteConnectionWorkingCopy open() {
		WizardDialog dialog = new WizardDialog(shell, this);
		dialog.setBlockOnOpen(true);
		if (dialog.open() == WizardDialog.OK) {
			return getConnection();
		}
		return null;
	}

	@Override
	public IRemoteConnectionWorkingCopy getConnection() {
		if (workingCopy == null) {
			try {
				workingCopy = connectionType.newConnection(page.getConnectionName());
			} catch (RemoteConnectionException e) {
				Activator.log(e.getStatus());
			}
		}
		return workingCopy;
	}

	@Override
	public void setConnection(IRemoteConnectionWorkingCopy connection) {
		workingCopy = connection;
	}

	@Override
	public void setConnectionName(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setInvalidConnectionNames(Set<String> names) {
		// TODO Auto-generated method stub

	}

}
