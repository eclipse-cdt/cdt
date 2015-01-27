/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.remote.internal.ui.views;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.internal.ui.Messages;
import org.eclipse.remote.internal.ui.RemoteUIPlugin;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;

/**
 * @since 2.0
 */
public class NewRemoteConnectionWizard extends Wizard {

	private final NewRemoteConnectionTypePage typePage;

	public NewRemoteConnectionWizard() {
		setWindowTitle(Messages.NewRemoteConnectionWizard_0);
		typePage = new NewRemoteConnectionTypePage();
		setForcePreviousAndNextButtons(true);
	}

	@Override
	public void addPages() {
		addPage(typePage);
	}

	@Override
	public boolean performFinish() {
		IRemoteUIConnectionWizard nextWizard = typePage.getNextWizard();
		if (nextWizard != null) {
			IRemoteConnectionWorkingCopy wc = nextWizard.getConnection();
			try {
				wc.save();
			} catch (RemoteConnectionException e) {
				RemoteUIPlugin.log(e);
				return false;
			}
			return true;
		} else {
			// what happened?
			return false;
		}
	}

	@Override
	public boolean canFinish() {
		// Need to move onto the new target wizard
		return false;
	}

}
