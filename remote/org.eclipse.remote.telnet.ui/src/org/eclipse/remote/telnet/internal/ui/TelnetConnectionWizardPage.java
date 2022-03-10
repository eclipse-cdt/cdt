/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial contribution
 *******************************************************************************/
package org.eclipse.remote.telnet.internal.ui;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.remote.telnet.core.TelnetConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class TelnetConnectionWizardPage extends WizardPage {

	private String host;
	private int port = TelnetConnection.DEFAULT_PORT;
	private int timeout = TelnetConnection.DEFAULT_TIMEOUT;

	private Text hostText;
	private Text portText;
	private Text timeoutText;

	protected TelnetConnectionWizardPage() {
		super(TelnetConnectionWizardPage.class.getName());
		setDescription(Messages.TelnetConnectionWizardPage_0);
		setTitle(Messages.TelnetConnectionWizardPage_1);
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));

		Label nameLabel = new Label(comp, SWT.NONE);
		nameLabel.setText(Messages.TelnetConnectionWizardPage_2);

		hostText = new Text(comp, SWT.BORDER | SWT.SINGLE);
		hostText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		hostText.setText(host != null ? host : ""); //$NON-NLS-1$
		hostText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				updateStatus();
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// Ignore
			}
		});

		Label portLabel = new Label(comp, SWT.NONE);
		portLabel.setText(Messages.TelnetConnectionWizardPage_3);

		portText = new Text(comp, SWT.BORDER | SWT.SINGLE);
		portText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		portText.setText(port < 0 ? "" : Integer.toString(port)); //$NON-NLS-1$
		portText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				updateStatus();
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// Ignore
			}
		});

		Label timeoutLabel = new Label(comp, SWT.NONE);
		timeoutLabel.setText(Messages.TelnetConnectionWizardPage_4);

		timeoutText = new Text(comp, SWT.BORDER | SWT.SINGLE);
		timeoutText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		timeoutText.setText(timeout < 0 ? "" : Integer.toString(timeout)); //$NON-NLS-1$
		timeoutText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				updateStatus();
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// Ignore
			}
		});

		setControl(comp);
		updateStatus();
	}

	private void updateStatus() {
		host = hostText.getText();
		try {
			port = Integer.parseInt(portText.getText());
		} catch (NumberFormatException e) {
			port = -1;
		}
		try {
			timeout = Integer.parseInt(timeoutText.getText());
		} catch (NumberFormatException e) {
			timeout = -1;
		}

		setPageComplete(!host.isEmpty());
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
}
