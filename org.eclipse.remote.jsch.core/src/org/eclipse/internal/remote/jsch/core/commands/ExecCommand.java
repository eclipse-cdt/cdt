package org.eclipse.internal.remote.jsch.core.commands;

import java.io.ByteArrayOutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.internal.remote.jsch.core.JSchConnection;
import org.eclipse.internal.remote.jsch.core.messages.Messages;
import org.eclipse.remote.core.exception.RemoteConnectionException;

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
			public String call() throws JSchException {
				getChannel().setCommand(fCommand);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				getChannel().setOutputStream(stream);
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
				return stream.toString();
			}
		};
		subMon.subTask(Messages.ExecCommand_Exec_command);
		return c.getResult(subMon.newChild(10));
	}
}
