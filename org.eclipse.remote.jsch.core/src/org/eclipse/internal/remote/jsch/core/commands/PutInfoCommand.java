package org.eclipse.internal.remote.jsch.core.commands;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.internal.remote.jsch.core.JSchConnection;
import org.eclipse.internal.remote.jsch.core.messages.Messages;
import org.eclipse.remote.core.exception.RemoteConnectionException;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public class PutInfoCommand extends AbstractRemoteCommand<Void> {

	private final IPath fRemotePath;
	private final IFileInfo fFileInfo;
	private final int fOptions;

	public PutInfoCommand(JSchConnection connection, IFileInfo info, int options, IPath path) {
		super(connection);
		fFileInfo = info;
		fOptions = options;
		fRemotePath = path;
	}

	@Override
	public Void getResult(IProgressMonitor monitor) throws RemoteConnectionException {
		final SubMonitor subMon = SubMonitor.convert(monitor, 30);

		FetchInfoCommand command = new FetchInfoCommand(getConnection(), fRemotePath);
		IFileInfo info = command.getResult(subMon.newChild(10));
		if ((fOptions & EFS.SET_ATTRIBUTES) != 0) {
			fFileInfo.setAttribute(EFS.ATTRIBUTE_READ_ONLY, info.getAttribute(EFS.ATTRIBUTE_READ_ONLY));
			fFileInfo.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, info.getAttribute(EFS.ATTRIBUTE_EXECUTABLE));
			chmod(getPermissions(fFileInfo), fRemotePath.toString(), subMon.newChild(10));
		}
		if ((fOptions & EFS.SET_LAST_MODIFIED) != 0) {
			long oldMTime = info.getLastModified();
			int newMTime = (int) (oldMTime / 1000);
			if (oldMTime != newMTime) {
				setMTime(newMTime, fRemotePath.toString(), subMon.newChild(10));
			}
		}
		return null;
	}

	private void chmod(final int permissions, final String path, IProgressMonitor monitor) throws RemoteConnectionException {
		final SubMonitor subMon = SubMonitor.convert(monitor, 10);
		SftpCallable<Void> c = new SftpCallable<Void>() {
			@Override
			public Void call() throws JSchException, SftpException {
				getChannel().chmod(permissions, path);
				return null;
			}
		};
		try {
			subMon.subTask(Messages.PutInfoCommand_Change_permissions);
			c.getResult(subMon.newChild(10));
		} catch (SftpException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}

	private void setMTime(final int mtime, final String path, IProgressMonitor monitor) throws RemoteConnectionException {
		final SubMonitor subMon = SubMonitor.convert(monitor, 10);
		SftpCallable<Void> c = new SftpCallable<Void>() {
			@Override
			public Void call() throws JSchException, SftpException {
				getChannel().setMtime(path, mtime);
				return null;
			}
		};
		try {
			subMon.subTask(Messages.PutInfoCommand_Set_modified_time);
			c.getResult(subMon.newChild(10));
		} catch (SftpException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}

	private int getPermissions(IFileInfo info) {
		int permissions = 0;
		if (info.getAttribute(EFS.ATTRIBUTE_OWNER_READ)) {
			permissions |= 0400;
		}
		if (info.getAttribute(EFS.ATTRIBUTE_OWNER_WRITE)) {
			permissions |= 0200;
		}
		if (info.getAttribute(EFS.ATTRIBUTE_OWNER_EXECUTE)) {
			permissions |= 0100;
		}
		if (info.getAttribute(EFS.ATTRIBUTE_GROUP_READ)) {
			permissions |= 0040;
		}
		if (info.getAttribute(EFS.ATTRIBUTE_GROUP_WRITE)) {
			permissions |= 0020;
		}
		if (info.getAttribute(EFS.ATTRIBUTE_GROUP_EXECUTE)) {
			permissions |= 0010;
		}
		if (info.getAttribute(EFS.ATTRIBUTE_OTHER_READ)) {
			permissions |= 0004;
		}
		if (info.getAttribute(EFS.ATTRIBUTE_OTHER_WRITE)) {
			permissions |= 0002;
		}
		if (info.getAttribute(EFS.ATTRIBUTE_OTHER_EXECUTE)) {
			permissions |= 0001;
		}
		return permissions;
	}
}
