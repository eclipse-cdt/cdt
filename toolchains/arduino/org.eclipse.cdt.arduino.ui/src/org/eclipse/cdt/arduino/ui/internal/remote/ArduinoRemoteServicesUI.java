package org.eclipse.cdt.arduino.ui.internal.remote;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.arduino.ui.internal.Activator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionType.Service;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.ui.IRemoteUIConnectionService;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.swt.widgets.Shell;

public class ArduinoRemoteServicesUI extends PlatformObject implements IRemoteUIConnectionService {

	private final IRemoteConnectionType connectionType;
	
	public ArduinoRemoteServicesUI(IRemoteConnectionType connectionType) {
		this.connectionType = connectionType;
	}

	@Override
	public void openConnectionWithProgress(Shell shell, IRunnableContext context, final IRemoteConnection connection) {
		try {
			context.run(false, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						connection.open(monitor);
					} catch (RemoteConnectionException e) {
						Activator.getDefault().getLog().log(e.getStatus());
					}
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e));
		}
	}

	@Override
	public IRemoteConnectionType getConnectionType() {
		return connectionType;
	}

	@Override
	public IRemoteUIConnectionWizard getConnectionWizard(Shell shell) {
		return new NewArduinoTargetWizard();
	}

	public static class Factory implements IRemoteConnectionType.Service.Factory {
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Service> T getService(IRemoteConnectionType connectionType, Class<T> service) {
			if (IRemoteUIConnectionService.class.equals(service)) {
				return (T) new ArduinoRemoteServicesUI(connectionType);
			}
			return null;
		}
	}

}
