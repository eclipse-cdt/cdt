/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.console;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.remote.core.IRemoteCommandShellService;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteProcessService;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.ui.widgets.RemoteConnectionWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class TerminalConsoleSettingsDialog extends Dialog {

	private static final String CONNECTION_TYPE = "connectionType"; //$NON-NLS-1$
	private static final String CONNECTION_NAME = "connectionName"; //$NON-NLS-1$
	private static final String ENCODING = "encoding"; //$NON-NLS-1$
	
	private RemoteConnectionWidget remoteConnWidget;
	private Combo encodingCombo;
	private String selectedEncoding;

	public TerminalConsoleSettingsDialog(IShellProvider parentShell) {
		this(parentShell.getShell());
	}

	public TerminalConsoleSettingsDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(2, false));

		IRemoteServicesManager manager = Activator.getService(IRemoteServicesManager.class);
		// TODO remove the remote process service once we get command shell available with ssh and local
		@SuppressWarnings("unchecked") List<IRemoteConnectionType> connTypes = manager.getConnectionTypesSupporting(
				IRemoteCommandShellService.class, IRemoteProcessService.class);

		remoteConnWidget = new RemoteConnectionWidget(composite, SWT.NONE, null, 0, connTypes);
		
		IDialogSettings settings = getDialogSettings();
		String initialId = settings.get(CONNECTION_TYPE);
		String initialName = settings.get(CONNECTION_NAME);
		if (initialId != null && initialName != null) {
			remoteConnWidget.setConnection(initialId, initialName);
		}
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		remoteConnWidget.setLayoutData(gd);
		remoteConnWidget.addSelectionListener(new ValidationSelectionListener());

		Label encodingLabel = new Label(composite, SWT.NONE);
		encodingLabel.setLayoutData(new GridData());
		encodingLabel.setText(ConsoleMessages.ENCODING);

		encodingCombo = new Combo(composite, SWT.READ_ONLY);
		encodingCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		int i = 0;
		String initialEncoding = settings.get(ENCODING);
		if (initialEncoding == null) {
			initialEncoding = Charset.defaultCharset().name();
		}
		for (String encoding : getEncodings()) {
			encodingCombo.add(encoding);
			if (encoding.equals(initialEncoding)) {
				selectedEncoding = encoding;
				encodingCombo.select(i);
			}
			i++;
		}
		encodingCombo.addSelectionListener(new ValidationSelectionListener());

		return composite;
	}

	private List<String> getEncodings(){
		List<String> encodings = new ArrayList<>(2);
		encodings.add("ISO-8859-1"); //$NON-NLS-1$
		encodings.add("UTF-8"); //$NON-NLS-1$

		String hostEncoding = Charset.defaultCharset().name();
		if (!encodings.contains(hostEncoding)) {
			encodings.add(hostEncoding);
		}
		return encodings;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		validateDialog();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(ConsoleMessages.SELECT_CONNECTION);
	}

	private void validateDialog() {
		IRemoteConnection connection = remoteConnWidget.getConnection();
		selectedEncoding = encodingCombo.getItem(encodingCombo.getSelectionIndex());
		if (connection != null && selectedEncoding != null) {
			getButton(OK).setEnabled(true);
		} else {
			getButton(OK).setEnabled(false);
		}
	}

	/**
	 * Get the remote connection selected in the dialog.
	 */
	public IRemoteConnection getRemoteConnection() {
		return remoteConnWidget.getConnection();
	}

	/**
	 * Get the encoding selected in the dialog.
	 */
	public String getEncoding() {
		return selectedEncoding;
	}

	class ValidationSelectionListener implements SelectionListener {
		@Override
		public void widgetSelected(SelectionEvent e) {
			validateDialog();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			validateDialog();
		}
	}
	
	private IDialogSettings getDialogSettings() {
		IDialogSettings result = Activator.getDefault().getDialogSettings().getSection(TerminalConsoleSettingsDialog.class.getName());

		if (result == null) {
			result = Activator.getDefault().getDialogSettings().addNewSection(TerminalConsoleSettingsDialog.class.getName());
		}

		return result;
		
	}
	
	@Override
	public int open() {
		int rc = super.open();
		if (rc == Window.OK) {
			// save the settings
			IDialogSettings settings = getDialogSettings();
			IRemoteConnection connection = getRemoteConnection();
			settings.put(CONNECTION_TYPE, connection.getConnectionType().getId());
			settings.put(CONNECTION_NAME, connection.getName());
			settings.put(ENCODING, getEncoding());
		}
		return rc;
	}
	
}
