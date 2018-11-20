/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
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
package org.eclipse.cdt.arduino.ui.internal.preferences;

import java.nio.file.Paths;

import org.eclipse.cdt.arduino.core.internal.ArduinoPreferences;
import org.eclipse.cdt.arduino.ui.internal.Messages;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ArduinoPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Text urlsText;
	private Text homeText;

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout());

		Composite homeComp = new Composite(control, SWT.NONE);
		homeComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		homeComp.setLayout(new GridLayout(3, false));

		Label label = new Label(homeComp, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		label.setText("Arduino home:");

		homeText = new Text(homeComp, SWT.BORDER);
		homeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		homeText.setText(ArduinoPreferences.getArduinoHome().toString());

		Button browse = new Button(homeComp, SWT.NONE);
		browse.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		browse.setText("Browse...");
		browse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog.setMessage("Select directory for the Arduino SDKs and toolchains.");
				String dir = dialog.open();
				if (dir != null) {
					homeText.setText(dir);
				}
			}
		});

		Text desc = new Text(control, SWT.READ_ONLY | SWT.WRAP);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		layoutData.widthHint = 500;
		desc.setLayoutData(layoutData);
		desc.setBackground(parent.getBackground());
		desc.setText(Messages.ArduinoPreferencePage_desc);

		urlsText = new Text(control, SWT.BORDER | SWT.MULTI);
		urlsText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		urlsText.setText(ArduinoPreferences.getBoardUrls());

		return control;
	}

	@Override
	public boolean performOk() {
		ArduinoPreferences.setBoardUrls(urlsText.getText());
		ArduinoPreferences.setArduinoHome(Paths.get(homeText.getText()));
		return true;
	}

	@Override
	protected void performDefaults() {
		String defaultHome = ArduinoPreferences.getDefaultArduinoHome();
		homeText.setText(defaultHome);
		ArduinoPreferences.setArduinoHome(Paths.get(defaultHome));

		String defaultBoardUrl = ArduinoPreferences.getDefaultBoardUrls();
		urlsText.setText(defaultBoardUrl);
		ArduinoPreferences.setBoardUrls(defaultBoardUrl);

		super.performDefaults();
	}

}
