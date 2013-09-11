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

import org.eclipse.internal.remote.jsch.core.JSchConnectionWorkingCopy;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.remote.core.IRemoteConnectionManager;

public class JSchConnectionWizard extends Wizard {

	private final JSchConnectionPage fPage;

	public JSchConnectionWizard(IRemoteConnectionManager connMgr) {
		fPage = new JSchConnectionPage(connMgr);
	}

	public JSchConnectionWizard(IRemoteConnectionManager connMgr, JSchConnectionWorkingCopy conn) {
		fPage = new JSchConnectionPage(connMgr, conn);
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

	public JSchConnectionWorkingCopy getConnection() {
		return fPage.getConnection();
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

}
