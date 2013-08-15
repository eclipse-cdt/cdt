package org.eclipse.internal.remote.jsch.core.commands;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.internal.remote.jsch.core.Activator;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

public class FetchInfoCommand extends AbstractRemoteCommand<IFileInfo> {

	private final IPath fRemotePath;

	public FetchInfoCommand(ChannelSftp channel, IPath path) {
		super(channel);
		fRemotePath = path;
	}

	@Override
	public IFileInfo getResult(IProgressMonitor monitor) throws CoreException {
		SubMonitor subMon = SubMonitor.convert(monitor, 20);
		SftpCallable<SftpATTRS> c = new SftpCallable<SftpATTRS>() {
			@Override
			public SftpATTRS call() throws JSchException, SftpException {
				return getChannel().lstat(fRemotePath.toString());
			}
		};
		SftpATTRS attrs;
		try {
			attrs = c.getResult("Fetch info", subMon.newChild(10));
			return convertToFileInfo(fRemotePath, attrs, subMon.newChild(10));
		} catch (SftpException e) {
			if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				FileInfo info = new FileInfo(fRemotePath.lastSegment());
				info.setExists(false);
				return info;
			}
			throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), e.getMessage(), e));
		}
	}
}
