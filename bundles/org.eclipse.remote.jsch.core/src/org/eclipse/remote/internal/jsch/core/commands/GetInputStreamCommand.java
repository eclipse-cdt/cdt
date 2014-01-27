package org.eclipse.remote.internal.jsch.core.commands;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.internal.jsch.core.JSchConnection;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public class GetInputStreamCommand extends AbstractRemoteCommand<InputStream> {
	private final IPath fRemotePath;

	public GetInputStreamCommand(JSchConnection connection, IPath path) {
		super(connection);
		fRemotePath = path;
	}

	@Override
	public InputStream getResult(IProgressMonitor monitor) throws RemoteConnectionException {
		final SubMonitor subMon = SubMonitor.convert(monitor, 10);
		SftpCallable<InputStream> c = new SftpCallable<InputStream>() {
			@Override
			public InputStream call() throws JSchException, SftpException, IOException {
				try {
					return getConnection().getSftpChannel().get(fRemotePath.toString(),
							new CommandProgressMonitor(getProgressMonitor()));
				} catch (RemoteConnectionException e) {
					throw new IOException(e.getMessage());
				}
			}
		};
		try {
			return c.getResult(subMon.newChild(10));
		} catch (SftpException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}
}
