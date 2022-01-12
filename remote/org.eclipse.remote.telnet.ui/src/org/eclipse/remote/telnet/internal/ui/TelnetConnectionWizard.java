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

import java.util.Set;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionHostService;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.swt.widgets.Shell;

public class TelnetConnectionWizard extends Wizard implements IRemoteUIConnectionWizard {

	private TelnetConnectionWizardPage page;
	private IRemoteConnectionWorkingCopy workingCopy;
	private final Shell shell;
	private final IRemoteConnectionType connectionType;

	public TelnetConnectionWizard(Shell shell, IRemoteConnectionType connectionType) {
		this.shell = shell;
		this.connectionType = connectionType;
	}

	@Override
	public void addPages() {
		page = new TelnetConnectionWizardPage();
		if (workingCopy != null) {
			IRemoteConnectionHostService hostSvc = workingCopy.getService(IRemoteConnectionHostService.class);
			if (hostSvc != null) {
				page.setHost(hostSvc.getHostname());
				page.setPort(hostSvc.getPort());
				page.setTimeout(hostSvc.getTimeout());
			}
		}
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		IRemoteConnection conn = getConnection();
		if (conn != null) {
			IRemoteConnectionHostService hostSvc = conn.getService(IRemoteConnectionHostService.class);
			if (hostSvc != null) {
				hostSvc.setHostname(page.getHost());
				hostSvc.setPort(page.getPort());
				hostSvc.setTimeout(page.getTimeout());
				return true;
			}
		}
		return false;
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
				workingCopy = connectionType.newConnection(page.getHost());
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
		// Ignored
	}

	@Override
	public void setInvalidConnectionNames(Set<String> names) {
		// Ignored
	}

}
