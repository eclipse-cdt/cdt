/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal.remote;

import java.util.Set;

import org.eclipse.cdt.arduino.core.internal.remote.ArduinoRemoteConnection;
import org.eclipse.cdt.arduino.ui.internal.Activator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class NewArduinoTargetWizard extends Wizard implements IRemoteUIConnectionWizard, INewWizard {

	private NewArduinoTargetWizardPage page;
	private IRemoteConnectionWorkingCopy workingCopy;
	private boolean isNewWizard;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		isNewWizard = true;
	}

	@Override
	public void addPages() {
		page = new NewArduinoTargetWizardPage();
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		if (getConnection() == null) {
			return false;
		}

		page.performFinish(workingCopy);

		if (isNewWizard) {
			// if called as a new wizard, we need to do the save
			try {
				workingCopy.save();
			} catch (RemoteConnectionException e) {
				Activator.log(e);
				return false;
			}
		}
		return true;
	}

	@Override
	public IRemoteConnectionWorkingCopy open() {
		return getConnection();
	}

	@Override
	public IRemoteConnectionWorkingCopy getConnection() {
		if (workingCopy == null) {
			IRemoteServicesManager remoteManager = Activator.getService(IRemoteServicesManager.class);
			IRemoteConnectionType connectionType = remoteManager.getConnectionType(ArduinoRemoteConnection.TYPE_ID);
			try {
				workingCopy = connectionType.newConnection(page.name);
			} catch (RemoteConnectionException e) {
				Activator.getDefault().getLog().log(e.getStatus());
				return null;
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
		// TODO Auto-generated method stub
	}

	@Override
	public void setInvalidConnectionNames(Set<String> names) {
		// TODO Auto-generated method stub
	}

}
