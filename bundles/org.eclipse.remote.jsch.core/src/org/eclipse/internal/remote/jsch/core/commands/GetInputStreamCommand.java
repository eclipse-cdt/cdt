package org.eclipse.internal.remote.jsch.core.commands;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.internal.remote.jsch.core.JSchConnection;
import org.eclipse.internal.remote.jsch.core.messages.Messages;
import org.eclipse.remote.core.exception.RemoteConnectionException;

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
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		SftpCallable<Void> c = new SftpCallable<Void>() {
			@Override
			public Void call() throws JSchException, SftpException, IOException {
				getChannel().get(fRemotePath.toString(), out, new CommandProgressMonitor(getProgressMonitor()));
				out.close();
				return null;
			}
		};
		try {
			subMon.subTask(Messages.GetInputStreamCommand_Get_input_stream);
			c.getResult(subMon.newChild(10));
			return new ByteArrayInputStream(out.toByteArray());
		} catch (SftpException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}
}
