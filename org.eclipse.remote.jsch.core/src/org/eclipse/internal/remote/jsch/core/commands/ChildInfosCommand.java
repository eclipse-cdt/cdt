package org.eclipse.internal.remote.jsch.core.commands;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.internal.remote.jsch.core.Activator;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public class ChildInfosCommand extends AbstractRemoteCommand<IFileInfo[]> {

	private final IPath fRemotePath;

	public ChildInfosCommand(ChannelSftp channel, IPath path) {
		super(channel);
		fRemotePath = path;
	}

	@Override
	public IFileInfo[] getResult(IProgressMonitor monitor) throws CoreException {
		final SubMonitor subMon = SubMonitor.convert(monitor, 20);

		Vector<LsEntry> files = getResult(fRemotePath.toString(), subMon.newChild(10));

		List<IFileInfo> result = new ArrayList<IFileInfo>();

		if (files != null && !subMon.isCanceled()) {
			Enumeration<LsEntry> enumeration = files.elements();
			while (enumeration.hasMoreElements() && !subMon.isCanceled()) {
				LsEntry entry = enumeration.nextElement();
				final String fileName = entry.getFilename();
				if (fileName.equals(".") || fileName.equals("..")) { //$NON-NLS-1$ //$NON-NLS-2$
					// Ignore parent and current dir entry.
					continue;
				}
				result.add(convertToFileInfo(fileName, fRemotePath, entry.getAttrs(), subMon.newChild(10)));
			}
		}

		return result.toArray(new IFileInfo[result.size()]);
	}

	private Vector<LsEntry> getResult(String path, IProgressMonitor monitor) throws CoreException {
		SftpCallable<Vector<LsEntry>> c = new SftpCallable<Vector<LsEntry>>() {
			@SuppressWarnings("unchecked")
			@Override
			public Vector<LsEntry> call() throws JSchException, SftpException {
				return getChannel().ls(fRemotePath.toString());
			}
		};
		try {
			return c.getResult("Get file attributes", monitor);
		} catch (SftpException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), e.getMessage(), e));
		}
	}
}
