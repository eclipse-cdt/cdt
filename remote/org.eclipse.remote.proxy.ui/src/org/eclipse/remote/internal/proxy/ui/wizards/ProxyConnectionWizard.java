/**
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.remote.internal.proxy.ui.wizards;

import java.util.Set;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.swt.widgets.Shell;

public class ProxyConnectionWizard extends Wizard implements IRemoteUIConnectionWizard {

	private final Shell fShell;
	private final ProxyConnectionPage fPage;

	public ProxyConnectionWizard(Shell shell, IRemoteConnectionType connectionType) {
		fShell = shell;
		fPage = new ProxyConnectionPage(connectionType);
	}

	@Override
	public void addPages() {
		super.addPages();
		addPage(fPage);
	}

	@Override
	public IRemoteConnectionWorkingCopy open() {
		WizardDialog dialog = new WizardDialog(fShell, this);
		dialog.setBlockOnOpen(true);
		if (dialog.open() == WizardDialog.OK) {
			return fPage.getConnection();
		}
		return null;
	}

	@Override
	public IRemoteConnectionWorkingCopy getConnection() {
		return fPage.getConnection();
	}

	@Override
	public boolean performCancel() {
		return true;
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	@Override
	public void setConnection(IRemoteConnectionWorkingCopy connection) {
		fPage.setConnection(connection);
	}

	@Override
	public void setConnectionName(String name) {
		fPage.setConnectionName(name);
	}

	@Override
	public void setInvalidConnectionNames(Set<String> names) {
		fPage.setInvalidConnectionNames(names);
	}

}
