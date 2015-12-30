/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal.remote;

import org.eclipse.cdt.arduino.ui.internal.Messages;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewArduinoTargetWizardPage extends WizardPage {

	String name;
	private Text nameText;

	BoardPropertyControl boardControl;

	public NewArduinoTargetWizardPage() {
		super("NewArduinoTargetPage"); //$NON-NLS-1$
		setDescription(Messages.NewArduinoTargetWizardPage_0);
		setTitle(Messages.NewArduinoTargetWizardPage_1);
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());

		Composite nameComp = new Composite(comp, SWT.NONE);
		nameComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		nameComp.setLayout(new GridLayout(2, false));

		Label nameLabel = new Label(nameComp, SWT.NONE);
		nameLabel.setText(Messages.NewArduinoTargetWizardPage_2);

		nameText = new Text(nameComp, SWT.BORDER | SWT.SINGLE);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameText.setText(Messages.NewArduinoTargetWizardPage_3);
		nameText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				name = nameText.getText();
				updateStatus();
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		boardControl = new BoardPropertyControl(comp, SWT.NONE);
		boardControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		boardControl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateStatus();
			}
		});

		setControl(comp);
		setPageComplete(false);
	}

	private void updateStatus() {
		setPageComplete(name != null && !name.isEmpty() && boardControl.getPortName() != null
				&& boardControl.getSelectedBoard() != null);
	}

	public void performFinish(IRemoteConnectionWorkingCopy workingCopy) {
		boardControl.apply(workingCopy);
	}

}
