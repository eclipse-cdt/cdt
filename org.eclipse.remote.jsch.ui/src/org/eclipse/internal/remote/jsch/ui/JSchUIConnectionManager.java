/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.internal.remote.jsch.ui;

import org.eclipse.internal.remote.jsch.core.JSchConnectionManager;
import org.eclipse.internal.remote.jsch.core.JSchConnectionWorkingCopy;
import org.eclipse.internal.remote.jsch.ui.wizards.JSchConnectionWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.ui.AbstractRemoteUIConnectionManager;
import org.eclipse.swt.widgets.Shell;

public class JSchUIConnectionManager extends AbstractRemoteUIConnectionManager {
	private final JSchConnectionManager fConnMgr;

	public JSchUIConnectionManager(IRemoteServices services) {
		fConnMgr = (JSchConnectionManager) services.getConnectionManager();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteUIConnectionManager#newConnection()
	 */
	public IRemoteConnectionWorkingCopy newConnection(Shell shell) {
		return newConnection(shell, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager#newConnection(org
	 * .eclipse.swt.widgets.Shell,
	 * org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager
	 * .IRemoteConnectionAttributeHint[], java.lang.String[])
	 */
	public IRemoteConnectionWorkingCopy newConnection(Shell shell, String[] attrHints, String[] attrHintValues) {
		JSchConnectionWizard wizard = new JSchConnectionWizard(fConnMgr);
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.setBlockOnOpen(true);
		if (dialog.open() == WizardDialog.OK) {
			return wizard.getConnection();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.ui.IRemoteUIConnectionManager#updateConnection(org.eclipse.swt.widgets.Shell,
	 * org.eclipse.remote.core.IRemoteConnectionWorkingCopy)
	 */
	public boolean updateConnection(Shell shell, IRemoteConnectionWorkingCopy connection) {
		if (connection instanceof JSchConnectionWorkingCopy) {
			JSchConnectionWorkingCopy jSchConn = (JSchConnectionWorkingCopy) connection;
			JSchConnectionWizard wizard = new JSchConnectionWizard(fConnMgr, jSchConn);
			WizardDialog dialog = new WizardDialog(shell, wizard);
			dialog.setBlockOnOpen(true);
			if (dialog.open() == WizardDialog.OK) {
				return true;
			}
		}
		return false;
	}

}
