package org.eclipse.internal.remote.jsch.core.commands;

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

public class DeleteCommand extends AbstractRemoteCommand<Void> {

	private final IPath fRemotePath;

	public DeleteCommand(ChannelSftp channel, IPath path) {
		super(channel);
		fRemotePath = path;
	}

	@Override
	public Void getResult(IProgressMonitor monitor) throws CoreException {
		final SubMonitor subMon = SubMonitor.convert(monitor, 10);
		SftpCallable<Void> c = new SftpCallable<Void>() {
			@Override
			public Void call() throws JSchException, SftpException {
				getChannel().rm(fRemotePath.toString());
				return null;
			}
		};
		try {
			c.getResult("Remove file", subMon.newChild(10));
		} catch (SftpException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), e.getMessage(), e));
		}
		return null;
	}
}
