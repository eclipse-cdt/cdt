package org.eclipse.remote.internal.jsch.core.commands;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.internal.jsch.core.JSchConnection;
import org.eclipse.remote.internal.jsch.core.messages.Messages;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

/**
 * The JSch implementation does not support multiple streams open on a single channel, so we must create a new channel for each
 * subsequent stream. This has the problem that there are usually only a limited number of channels that can be opened
 * simultaneously, so it is possible that this call will fail unless the open streams are closed first.
 *
 * This code will use the initial (command) channel first, or if that is already being used, will open a new stream. It must be
 * careful not to close the command stream as other threads may still be using it.
 */
public class GetInputStreamCommand extends AbstractRemoteCommand<InputStream> {
	private final IPath fRemotePath;

	private static ChannelSftp commandChannel;
	private ChannelSftp thisChannel;

	public GetInputStreamCommand(JSchConnection connection, IPath path) {
		super(connection);
		fRemotePath = path;
	}

	@Override
	public InputStream getResult(IProgressMonitor monitor) throws RemoteConnectionException {
		final SubMonitor subMon = SubMonitor.convert(monitor, 10);

		final SftpCallable<InputStream> c = new SftpCallable<InputStream>() {
			private ChannelSftp newChannel() throws IOException {
				synchronized (GetInputStreamCommand.class) {
					if (commandChannel != null) {
						try {
							thisChannel = getConnection().newSftpChannel();
							return thisChannel;
						} catch (RemoteConnectionException e) {
							throw new IOException(e.getMessage());
						}
					}
					thisChannel = commandChannel = getChannel();
					return commandChannel;
				}
			}

			@Override
			public InputStream call() throws JSchException, SftpException, IOException {
				return newChannel().get(fRemotePath.toString(),
						new CommandProgressMonitor(
								NLS.bind(Messages.GetInputStreamCommand_Receiving, fRemotePath.toString()),
								getProgressMonitor()));
			}
		};
		try {
			final InputStream stream = c.getResult(subMon.newChild(10));
			return new InputStream() {
				@Override
				public int read() throws IOException {
					return stream.read();
				}

				@Override
				public void close() throws IOException {
					stream.close();
					synchronized (GetInputStreamCommand.class) {
						if (thisChannel != commandChannel) {
							thisChannel.disconnect();
						} else {
							commandChannel = null;
						}
					}
				}

				@Override
				public int read(byte[] b) throws IOException {
					return stream.read(b);
				}

				@Override
				public int read(byte[] b, int off, int len) throws IOException {
					return stream.read(b, off, len);
				}

				@Override
				public long skip(long n) throws IOException {
					return stream.skip(n);
				}

				@Override
				public int available() throws IOException {
					return stream.available();
				}

				@Override
				public synchronized void mark(int readlimit) {
					stream.mark(readlimit);
				}

				@Override
				public synchronized void reset() throws IOException {
					stream.reset();
				}

				@Override
				public boolean markSupported() {
					return stream.markSupported();
				}
			};
		} catch (SftpException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}
}
