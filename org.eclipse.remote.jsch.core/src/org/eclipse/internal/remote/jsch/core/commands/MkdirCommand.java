package org.eclipse.internal.remote.jsch.core.commands;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.internal.remote.jsch.core.JSchConnection;
import org.eclipse.internal.remote.jsch.core.messages.Messages;
import org.eclipse.remote.core.exception.RemoteConnectionException;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public class MkdirCommand extends AbstractRemoteCommand<Void> {

	private final IPath fRemotePath;

	public MkdirCommand(JSchConnection connection, IPath path) {
		super(connection);
		fRemotePath = path;
	}

	@Override
	public Void getResult(IProgressMonitor monitor) throws RemoteConnectionException {
		createDirectory(fRemotePath, monitor);
		return null;
	}

	private void createDirectory(IPath path, IProgressMonitor monitor) throws RemoteConnectionException {
		final SubMonitor subMon = SubMonitor.convert(monitor, 20);

		/*
		 * Recursively create parent directories
		 */
		FetchInfoCommand command = new FetchInfoCommand(getConnection(), path.removeLastSegments(1));
		IFileInfo info = command.getResult(subMon.newChild(10));
		if (!info.exists()) {
			createDirectory(path.removeLastSegments(1), subMon.newChild(10));
		}

		/*
		 * Now create directory
		 */
		SftpCallable<Void> c = new SftpCallable<Void>() {
			@Override
			public Void call() throws JSchException, SftpException {
				getChannel().mkdir(fRemotePath.toString());
				return null;
			}
		};
		try {
			subMon.subTask(Messages.MkdirCommand_Create_directory);
			c.getResult(subMon.newChild(10));
		} catch (SftpException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}
}
