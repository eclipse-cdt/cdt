/**
 * Copyright (c) 2013 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.internal.remote.jsch.ui.wizards;

import java.util.Set;

import org.eclipse.internal.remote.jsch.core.JSchConnectionWorkingCopy;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.swt.widgets.Shell;

public class JSchConnectionWizard extends Wizard implements IRemoteUIConnectionWizard {
	private final Shell fShell;
	private final JSchConnectionPage fPage;

	public JSchConnectionWizard(Shell shell, IRemoteConnectionManager connMgr) {
		fShell = shell;
		fPage = new JSchConnectionPage(connMgr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		super.addPages();
		addPage(fPage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.ui.IRemoteUIConnectionWizard#getWorkingCopy()
	 */
	public IRemoteConnectionWorkingCopy open() {
		WizardDialog dialog = new WizardDialog(fShell, this);
		dialog.setBlockOnOpen(true);
		if (dialog.open() == WizardDialog.OK) {
			return fPage.getConnection();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performCancel()
	 */
	@Override
	public boolean performCancel() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.ui.IRemoteUIConnectionWizard#setConnection(org.eclipse.remote.core.IRemoteConnectionWorkingCopy)
	 */
	public void setConnection(IRemoteConnectionWorkingCopy connection) {
		fPage.setConnection((JSchConnectionWorkingCopy) connection);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.ui.IRemoteUIConnectionWizard#setConnectionName(java.lang.String)
	 */
	public void setConnectionName(String name) {
		fPage.setConnectionName(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.ui.IRemoteUIConnectionWizard#setInvalidConnectionNames(java.util.Set)
	 */
	public void setInvalidConnectionNames(Set<String> names) {
		fPage.setInvalidConnectionNames(names);
	}
}
