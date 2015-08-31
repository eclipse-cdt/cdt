/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal.remote;

import java.util.Set;

import org.eclipse.cdt.arduino.core.internal.board.ArduinoBoard;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPackage;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPlatform;
import org.eclipse.cdt.arduino.core.internal.remote.ArduinoRemoteConnection;
import org.eclipse.cdt.arduino.ui.internal.Activator;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;

public class NewArduinoTargetWizard extends Wizard implements IRemoteUIConnectionWizard {

	private NewArduinoTargetWizardPage page;
	private IRemoteConnectionWorkingCopy workingCopy;

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

		workingCopy.setAttribute(ArduinoRemoteConnection.PORT_NAME, page.portName);

		ArduinoBoard board = page.board;
		workingCopy.setAttribute(ArduinoRemoteConnection.BOARD_NAME, board.getName());
		ArduinoPlatform platform = board.getPlatform();
		workingCopy.setAttribute(ArduinoRemoteConnection.PLATFORM_NAME, platform.getName());
		ArduinoPackage pkg = platform.getPackage();
		workingCopy.setAttribute(ArduinoRemoteConnection.PACKAGE_NAME, pkg.getName());

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
