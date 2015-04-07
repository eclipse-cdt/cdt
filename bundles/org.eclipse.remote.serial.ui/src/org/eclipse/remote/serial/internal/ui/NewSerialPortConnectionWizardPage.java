/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - initial contribution
 *******************************************************************************/
package org.eclipse.remote.serial.internal.ui;

import java.io.IOException;

import org.eclipse.cdt.serial.BaudRate;
import org.eclipse.cdt.serial.ByteSize;
import org.eclipse.cdt.serial.Parity;
import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.cdt.serial.StopBits;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewSerialPortConnectionWizardPage extends WizardPage {

	String name;
	String portName;
	int baudRateIndex;
	int byteSizeIndex;
	int parityIndex;
	int stopBitsIndex;

	private String[] portNames;

	private Text nameText;
	private Combo portCombo;
	private Combo baudRateCombo;
	private Combo byteSizeCombo;
	private Combo parityCombo;
	private Combo stopBitsCombo;

	protected NewSerialPortConnectionWizardPage() {
		super(NewSerialPortConnectionWizardPage.class.getName());
		setDescription(Messages.NewSerialPortConnectionWizardPage_Description);
		setTitle(Messages.NewSerialPortConnectionWizardPage_Title);
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));

		Label nameLabel = new Label(comp, SWT.NONE);
		nameLabel.setText(Messages.NewSerialPortConnectionWizardPage_NameLabel);

		nameText = new Text(comp, SWT.BORDER | SWT.SINGLE);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameText.setText(""); //$NON-NLS-1$
		nameText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				updateStatus();
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		Label portLabel = new Label(comp, SWT.NONE);
		portLabel.setText(Messages.NewSerialPortConnectionWizardPage_PortLabel);

		portCombo = new Combo(comp, SWT.READ_ONLY);
		portCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		try {
			portNames = SerialPort.list();
		} catch (IOException e) {
			Activator.log(e);
		}
		for (String portName : portNames) {
			portCombo.add(portName);
		}
		portCombo.select(0);
		portCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateStatus();
			}
		});

		Label baudRateLabel = new Label(comp, SWT.NONE);
		baudRateLabel.setText(Messages.NewSerialPortConnectionWizardPage_BaudRateLabel);

		baudRateCombo = new Combo(comp, SWT.READ_ONLY);
		baudRateCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (String baudRateStr : BaudRate.getStrings()) {
			baudRateCombo.add(baudRateStr);
		}
		// TODO remember the last one
		baudRateCombo.select(BaudRate.getStringIndex(BaudRate.getDefault()));
		baudRateCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateStatus();
			}
		});

		Label byteSizeLabel = new Label(comp, SWT.NONE);
		byteSizeLabel.setText(Messages.NewSerialPortConnectionWizardPage_ByteSizeLabel);

		byteSizeCombo = new Combo(comp, SWT.READ_ONLY);
		byteSizeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (String byteSizeStr : ByteSize.getStrings()) {
			byteSizeCombo.add(byteSizeStr);
		}
		byteSizeCombo.select(ByteSize.getStringIndex(ByteSize.getDefault()));
		byteSizeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateStatus();
			}
		});

		Label parityLabel = new Label(comp, SWT.NONE);
		parityLabel.setText(Messages.NewSerialPortConnectionWizardPage_ParityLabel);

		parityCombo = new Combo(comp, SWT.READ_ONLY);
		parityCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (String parityStr : Parity.getStrings()) {
			parityCombo.add(parityStr);
		}
		parityCombo.select(Parity.getStringIndex(Parity.getDefault()));
		parityCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateStatus();
			}
		});

		Label stopBitsLabel = new Label(comp, SWT.NONE);
		stopBitsLabel.setText(Messages.NewSerialPortConnectionWizardPage_StopBitsLabel);

		stopBitsCombo = new Combo(comp, SWT.READ_ONLY);
		stopBitsCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (String stopBitsStr : StopBits.getStrings()) {
			stopBitsCombo.add(stopBitsStr);
		}
		stopBitsCombo.select(StopBits.getStringIndex(StopBits.getDefault()));
		stopBitsCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateStatus();
			}
		});

		setControl(comp);
		updateStatus();
	}

	private void updateStatus() {
		name = nameText.getText();

		int portIndex = portCombo.getSelectionIndex();
		portName = portIndex < 0 ? null : portNames[portIndex];

		baudRateIndex = baudRateCombo.getSelectionIndex();
		byteSizeIndex = byteSizeCombo.getSelectionIndex();
		parityIndex = parityCombo.getSelectionIndex();
		stopBitsIndex = stopBitsCombo.getSelectionIndex();

		setPageComplete(!name.isEmpty() && portName != null);
	}

}
