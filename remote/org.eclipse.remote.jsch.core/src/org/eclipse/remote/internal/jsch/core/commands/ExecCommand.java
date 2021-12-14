package org.eclipse.remote.internal.jsch.core.commands;

import java.io.ByteArrayOutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.internal.jsch.core.JSchConnection;
import org.eclipse.remote.internal.jsch.core.messages.Messages;

import com.jcraft.jsch.JSchException;

public class ExecCommand extends AbstractRemoteCommand<String> {

	private String fCommand;

	public ExecCommand(JSchConnection connection) {
		super(connection);
	}

	public ExecCommand setCommand(String command) {
		fCommand = command;
		return this;
	}

	@Override
	public String getResult(IProgressMonitor monitor) throws RemoteConnectionException {
		final SubMonitor subMon = SubMonitor.convert(monitor, 10);
		ExecCallable<String> c = new ExecCallable<String>() {
			@Override
			public String call() throws JSchException, RemoteConnectionException {
				getChannel().setCommand(fCommand);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				ByteArrayOutputStream err = new ByteArrayOutputStream();
				getChannel().setOutputStream(stream);
				getChannel().setErrStream(err);
				getChannel().connect();
				while (!getChannel().isClosed() && !getProgressMonitor().isCanceled()) {
					synchronized (this) {
						try {
							wait(100);
						} catch (InterruptedException e) {
							// Ignore
						}
					}
				}
				if (getProgressMonitor().isCanceled()) {
					return ""; //$NON-NLS-1$
				}
				if (getChannel().getExitStatus()!=0) {
					throw new RemoteConnectionException(err.toString());
				}
				return stream.toString();
			}
		};
		subMon.subTask(NLS.bind(Messages.ExecCommand_Exec_command, fCommand));
		return c.getResult(subMon.newChild(10));
	}
}
