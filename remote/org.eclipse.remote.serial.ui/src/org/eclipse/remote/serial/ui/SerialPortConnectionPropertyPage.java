/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems, and others.
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

import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.serial.core.ISerialPortService;
import org.eclipse.remote.serial.internal.ui.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

public class SerialPortConnectionPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {

	protected SerialPortConnectionBlock block;
	protected IRemoteConnectionWorkingCopy workingCopy;

	public SerialPortConnectionPropertyPage() {
		super();
		block = new SerialPortConnectionBlock();
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));

		block.addUpdateListener(block.new SerialBlockUpdateListener() {

			@Override
			public void update() {
				setValid(block.isComplete());
			}

		});

		IRemoteConnection remoteConnection = getElement().getAdapter(IRemoteConnection.class);
		if (remoteConnection != null)
			workingCopy = remoteConnection.getWorkingCopy();
		else
			workingCopy = null;

		block.createBlock(comp, workingCopy);
		return comp;
	}

	@Override
	public boolean performOk() {
		if (workingCopy != null) {

			workingCopy.setName(block.getConnectionName());
			workingCopy.setAttribute(ISerialPortService.PORT_NAME_ATTR, block.getPortName());
			workingCopy.setAttribute(ISerialPortService.BAUD_RATE_ATTR, Integer.toString(block.getBaudRateIndex()));
			workingCopy.setAttribute(ISerialPortService.BYTE_SIZE_ATTR, Integer.toString(block.getByteSizeIndex()));
			workingCopy.setAttribute(ISerialPortService.PARITY_ATTR, Integer.toString(block.getParityIndex()));
			workingCopy.setAttribute(ISerialPortService.STOP_BITS_ATTR, Integer.toString(block.getStopBitsIndex()));
			try {
				workingCopy.save();
			} catch (RemoteConnectionException e) {
				Activator.log(e);
				return false;
			}

		}

		return true;
	}

}
