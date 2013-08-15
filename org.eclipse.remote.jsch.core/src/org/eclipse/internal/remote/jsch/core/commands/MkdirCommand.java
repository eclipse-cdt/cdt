package org.eclipse.internal.remote.jsch.core.commands;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.internal.remote.jsch.core.Activator;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public class MkdirCommand extends AbstractRemoteCommand<Void> {

	private final IPath fRemotePath;

	public MkdirCommand(ChannelSftp channel, IPath path) {
		super(channel);
		fRemotePath = path;
	}

	@Override
	public Void getResult(IProgressMonitor monitor) throws CoreException {
		createDirectory(fRemotePath, monitor);
		return null;
	}

	private void createDirectory(IPath path, IProgressMonitor monitor) throws CoreException {
		final SubMonitor subMon = SubMonitor.convert(monitor, 20);

		/*
		 * Recursively create parent directories
		 */
		FetchInfoCommand command;
		try {
			command = new FetchInfoCommand(getSftpChannel(), path.removeLastSegments(1));
		} catch (JSchException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), e.getMessage(), e));
		}
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
			c.getResult("Create directory", subMon.newChild(10));
		} catch (SftpException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), e.getMessage(), e));
		}
	}
}
