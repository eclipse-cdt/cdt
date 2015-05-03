package org.eclipse.cdt.arduino.ui.internal.remote;

import java.util.Set;

import org.eclipse.cdt.arduino.core.IArduinoRemoteConnection;
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
		
		workingCopy.setAttribute(IArduinoRemoteConnection.PORT_NAME, page.portName);
		workingCopy.setAttribute(IArduinoRemoteConnection.BOARD_ID, page.board.getId());
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
			IRemoteConnectionType connectionType = remoteManager.getConnectionType(IArduinoRemoteConnection.TYPE_ID);
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
