package org.eclipse.remote.internal.jsch.core.commands;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.internal.jsch.core.JSchConnection;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public class PutInfoCommand extends AbstractRemoteCommand<Void> {
	private static final int S_IRUSR = 0400; // owner has read permission
	private static final int S_IWUSR = 0200; // owner has write permission
	private static final int S_IXUSR = 0100; // owner has execute permission
	private static final int S_IRGRP = 0040; // group has read permission
	private static final int S_IWGRP = 0020; // group has write permission
	private static final int S_IXGRP = 0010; // group has execute permission
	private static final int S_IROTH = 0004; // others have read permission
	private static final int S_IWOTH = 0002; // others have write permission
	private static final int S_IXOTH = 0001; // others have execute permission

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
		if ((fOptions & EFS.SET_ATTRIBUTES) != 0) {
			chmod(getPermissions(fFileInfo), fRemotePath.toString(), subMon.newChild(10));
		}
		if ((fOptions & EFS.SET_LAST_MODIFIED) != 0) {
			IFileInfo info = command.getResult(subMon.newChild(10));
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
			c.getResult(subMon.newChild(10));
		} catch (SftpException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}

	private int getPermissions(IFileInfo info) {
		int permissions = 0;
		if (info.getAttribute(EFS.ATTRIBUTE_OWNER_READ)) {
			permissions |= S_IRUSR;
		}
		if (info.getAttribute(EFS.ATTRIBUTE_OWNER_WRITE)) {
			permissions |= S_IWUSR;
		}
		if (info.getAttribute(EFS.ATTRIBUTE_OWNER_EXECUTE)) {
			permissions |= S_IXUSR;
		}
		if (info.getAttribute(EFS.ATTRIBUTE_GROUP_READ)) {
			permissions |= S_IRGRP;
		}
		if (info.getAttribute(EFS.ATTRIBUTE_GROUP_WRITE)) {
			permissions |= S_IWGRP;
		}
		if (info.getAttribute(EFS.ATTRIBUTE_GROUP_EXECUTE)) {
			permissions |= S_IXGRP;
		}
		if (info.getAttribute(EFS.ATTRIBUTE_OTHER_READ)) {
			permissions |= S_IROTH;
		}
		if (info.getAttribute(EFS.ATTRIBUTE_OTHER_WRITE)) {
			permissions |= S_IWOTH;
		}
		if (info.getAttribute(EFS.ATTRIBUTE_OTHER_EXECUTE)) {
			permissions |= S_IXOTH;
		}
		return permissions;
	}
}
