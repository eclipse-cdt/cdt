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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.remote.serial.internal.ui.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class NewSerialPortConnectionWizardPage extends WizardPage {

	protected SerialPortConnectionBlock block;

	public NewSerialPortConnectionWizardPage() {
		super(NewSerialPortConnectionWizardPage.class.getName());
		setDescription(Messages.NewSerialPortConnectionWizardPage_Description);
		setTitle(Messages.NewSerialPortConnectionWizardPage_Title);
		block = new SerialPortConnectionBlock();
		block.addUpdateListener(block.new SerialBlockUpdateListener() {

			@Override
			public void update() {
				setPageComplete(block.isComplete());
			}

		});
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		block.createBlock(comp, null);
		setControl(comp);
	}

	public String getConnectionName() {
		return block.getConnectionName();
	}

	public String getPortName() {
		return block.getPortName();
	}

	public int getBaudRateIndex() {
		return block.getBaudRateIndex();
	}

	public int getByteSizeIndex() {
		return block.getByteSizeIndex();
	}

	public int getParityIndex() {
		return block.getParityIndex();
	}

	public int getStopBitsIndex() {
		return block.getStopBitsIndex();
	}

}
