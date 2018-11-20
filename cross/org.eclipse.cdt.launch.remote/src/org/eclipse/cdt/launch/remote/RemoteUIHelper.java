/********************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wainer S. Moschetta (IBM) - initial API and implementation
 ********************************************************************************/
package org.eclipse.cdt.launch.remote;

import java.util.ArrayList;

import org.eclipse.cdt.internal.launch.remote.Activator;
import org.eclipse.cdt.internal.launch.remote.Messages;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.remote.core.IRemoteCommandShellService;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.ui.IRemoteUIConnectionService;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class RemoteUIHelper {
	/**
	 * Open dialog to edit a remote connection.
	 *
	 * @param connection - the remote connection
	 * @param shell - the shell
	 */
	public static void editConnectionDialog(IRemoteConnection connection, Shell shell) {
		if (connection == null) {
			return;
		}
		IRemoteUIConnectionService uiConnServices = connection.getConnectionType()
				.getService(IRemoteUIConnectionService.class);
		IRemoteUIConnectionWizard wizard = uiConnServices.getConnectionWizard(shell);
		wizard.setConnection(connection.getWorkingCopy());
		IRemoteConnectionWorkingCopy connCopy = wizard.open();
		if (connCopy != null) {
			try {
				connCopy.save();
			} catch (RemoteConnectionException e) {
				logError(e);
			}
		}
	}

	/**
	 * Open dialog for user to create a new connection.
	 *
	 * @param shell - the shell
	 */
	public static void newConnectionDialog(Shell shell) {
		/*
		 * Evoke native new connection wizard and save connection
		 * 	if created by the user.
		 */
		class NewRemoteConnectionDialog extends Dialog {
			private String title;
			private Combo fConnSelection;
			private IRemoteServicesManager manager;

			protected NewRemoteConnectionDialog(Shell parentShell, String windowTitle) {
				super(parentShell);
				title = windowTitle;
				manager = Activator.getService(IRemoteServicesManager.class);
			}

			@Override
			protected void configureShell(Shell newShell) {
				super.configureShell(newShell);
				newShell.setText(title);
			}

			@Override
			protected Control createDialogArea(Composite parent) {
				Composite composite = (Composite) super.createDialogArea(parent);

				Label label = new Label(composite, SWT.WRAP);
				label.setText(Messages.RemoteCMainTab_New_conntype_combo_label);
				GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL
						| GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
				data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
				label.setLayoutData(data);
				label.setFont(parent.getFont());
				fConnSelection = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
				ArrayList<String> suitableConnections = new ArrayList<>();
				for (IRemoteConnectionType type : manager.getAllConnectionTypes()) {
					if (type.canAdd()
							&& type.getConnectionServices().contains(IRemoteCommandShellService.class.getName())) {
						fConnSelection.setData(type.getName(), type.getId());
						suitableConnections.add(type.getName());
					}
				}
				fConnSelection.setItems(suitableConnections.toArray(new String[0]));
				fConnSelection.select(0);

				applyDialogFont(composite);
				return composite;
			}

			@Override
			protected void buttonPressed(int buttonId) {
				if (buttonId == IDialogConstants.OK_ID) {
					String connTypeId = (String) fConnSelection.getData(fConnSelection.getText());
					IRemoteConnectionType connType = manager.getConnectionType(connTypeId);
					IRemoteUIConnectionService fUIConnectionManager = connType
							.getService(IRemoteUIConnectionService.class);
					IRemoteUIConnectionWizard wizard = fUIConnectionManager.getConnectionWizard(this.getShell());

					IRemoteConnectionWorkingCopy wc = wizard.open();
					if (wc != null) {
						try {
							wc.save();
						} catch (RemoteConnectionException e) {
							logError(e);
						}
					}
				}
				super.buttonPressed(buttonId);
			}
		}

		NewRemoteConnectionDialog dlg = new NewRemoteConnectionDialog(shell, Messages.RemoteCMainTab_New_title);
		dlg.setBlockOnOpen(true);
		dlg.open();
	}

	/**
	 * Get a property associated with the connection.
	 *
	 * @param connection - the connection
	 * @param property - the property's name
	 * @return the property's value or empty string if it is not set.
	 */
	public static String getConnectionProperty(IRemoteConnection connection, String property) {
		String key = IRemoteConnectionHostConstants.PI_REMOTE_CDT + "." //$NON-NLS-1$
				+ property;
		return connection.getAttribute(key);
	}

	/**
	 * Associate a property with the connection.
	 *
	 * @param connection - the connection
	 * @param property - the property's name
	 * @param value the property's value
	 */
	public static void setConnectionProperty(IRemoteConnection connection, String property, String value) {
		String key = IRemoteConnectionHostConstants.PI_REMOTE_CDT + "." //$NON-NLS-1$
				+ property;
		IRemoteConnectionWorkingCopy wc = connection.getWorkingCopy();
		wc.setAttribute(key, value);
		try {
			wc.save();
		} catch (RemoteConnectionException e) {
			logError(e);
		}
	}

	private static void logError(Exception e) {
		Plugin plugin = Activator.getDefault();
		ILog logger = plugin.getLog();
		logger.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
	}
}
