package org.eclipse.remote.internal.core.services.local;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Path;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnection.Service;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteProcessService;

public class LocalProcessService implements IRemoteProcessService {

	private final IRemoteConnection remoteConnection;
	private String workingDirectory;

	public LocalProcessService(IRemoteConnection remoteConnection) {
		this.remoteConnection = remoteConnection;
	}

	public static class Factory implements IRemoteProcessService.Factory {
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Service> T getService(IRemoteConnection remoteConnection, Class<T> service) {
			if (IRemoteProcessService.class.equals(service)) {
				return (T) new LocalProcessService(remoteConnection);
			}
			return null;
		}
	}

	@Override
	public IRemoteConnection getRemoteConnection() {
		return remoteConnection;
	}

	@Override
	public Map<String, String> getEnv() {
		return System.getenv();
	}

	@Override
	public String getEnv(String name) {
		return System.getenv(name);
	}

	@Override
	public IRemoteProcessBuilder getProcessBuilder(List<String> command) {
		return new LocalProcessBuilder(remoteConnection, command);
	}

	@Override
	public IRemoteProcessBuilder getProcessBuilder(String... command) {
		return new LocalProcessBuilder(remoteConnection, command);
	}

	@Override
	public String getWorkingDirectory() {
		if (workingDirectory == null) {
			workingDirectory = System.getProperty("user.home"); //$NON-NLS-1$
			if (workingDirectory == null) {
				workingDirectory = System.getProperty("user.dir"); //$NON-NLS-1$
				if (workingDirectory == null) {
					workingDirectory = Path.ROOT.toOSString();
				}
			}
		}
		return workingDirectory;
	}

	@Override
	public void setWorkingDirectory(String path) {
		workingDirectory = path;
	}

}
