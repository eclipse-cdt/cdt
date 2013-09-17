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
import org.eclipse.internal.remote.jsch.ui.wizards.JSchConnectionWizard;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.ui.AbstractRemoteUIConnectionManager;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.swt.widgets.Shell;

public class JSchUIConnectionManager extends AbstractRemoteUIConnectionManager {
	private final JSchConnectionManager fConnMgr;

	public JSchUIConnectionManager(IRemoteServices services) {
		fConnMgr = (JSchConnectionManager) services.getConnectionManager();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.ui.IRemoteUIConnectionManager#getConnectionWizard(org.eclipse.swt.widgets.Shell)
	 */
	public IRemoteUIConnectionWizard getConnectionWizard(Shell shell) {
		return new JSchConnectionWizard(shell, fConnMgr);
	}
}
