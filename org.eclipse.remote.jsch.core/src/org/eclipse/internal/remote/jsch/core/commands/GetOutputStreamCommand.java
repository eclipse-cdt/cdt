package org.eclipse.internal.remote.jsch.core.commands;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.internal.remote.jsch.core.JSchConnection;
import org.eclipse.remote.core.exception.RemoteConnectionException;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public class GetOutputStreamCommand extends AbstractRemoteCommand<OutputStream> {
	private final IPath fRemotePath;
	private final int fOptions;
	private boolean fIsClosed;

	public GetOutputStreamCommand(JSchConnection connection, int options, IPath path) {
		super(connection);
		fRemotePath = path;
		fOptions = options;
		fIsClosed = false;
	}

	@Override
	public OutputStream getResult(IProgressMonitor monitor) throws RemoteConnectionException {
		final SubMonitor subMon = SubMonitor.convert(monitor, 10);
		return new ByteArrayOutputStream() {
			@Override
			public void close() throws IOException {
				if (!fIsClosed) {
					super.close();
					final InputStream input = new ByteArrayInputStream(this.toByteArray());
					try {
						SftpCallable<Integer> c = new SftpCallable<Integer>() {
							@Override
							public Integer call() throws JSchException, SftpException, IOException {
								try {
									int mode = ChannelSftp.OVERWRITE;
									if ((fOptions & EFS.APPEND) != 0) {
										mode = ChannelSftp.APPEND;
									}
									getChannel().put(input, fRemotePath.toString(),
											new CommandProgressMonitor(getProgressMonitor()), mode);
									input.close();
								} finally {
									fIsClosed = true;
								}
								return 0;
							}
						};
						c.getResult(subMon.newChild(10));
					} catch (SftpException e) {
						throw new IOException(e.getMessage());
					} catch (CoreException e) {
						throw new IOException(e.getMessage());
					}
				}
			}
		};
	}
}
