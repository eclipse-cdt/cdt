/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.remote.core;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.remote.internal.core.messages.Messages;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteResource;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteProcessAdapter;
import org.eclipse.remote.core.RemoteServices;

public class RemoteCommandLauncher implements ICommandLauncher {
	private final ICommandLauncher fLocalLauncher = new CommandLauncher();
	private boolean fShowCommand;
	private String[] fCommandArgs;
	private IRemoteConnection fConnection;
	private IRemoteProcess fRemoteProcess;
	private final Properties fEnvironment = new Properties();

	/**
	 * The number of milliseconds to pause between polling.
	 */
	private static final long DELAY = 50L;

	/**
	 * Constructs a command array that will be passed to the process
	 */
	private String[] constructCommandArray(String command, String[] commandArgs) {
		String[] args = new String[1 + commandArgs.length];
		args[0] = command;
		System.arraycopy(commandArgs, 0, args, 1, commandArgs.length);
		return args;
	}

	@Override
	public Process execute(IPath commandPath, String[] args, String[] env, IPath workingDirectory, IProgressMonitor monitor)
			throws CoreException {
		if (fLocalLauncher.getProject() != null) {
			IRemoteResource remRes = (IRemoteResource) fLocalLauncher.getProject().getAdapter(IRemoteResource.class);
			if (remRes != null) {
				URI uri = remRes.getActiveLocationURI();
				IRemoteServices remServices = RemoteServices.getRemoteServices(uri);
				if (remServices != null) {
					fConnection = remServices.getConnectionManager().getConnection(uri);
					if (fConnection != null) {
						parseEnvironment(env);
						fCommandArgs = constructCommandArray(commandPath.toOSString(), args);
						IRemoteProcessBuilder processBuilder = fConnection.getProcessBuilder(fCommandArgs);
						if (workingDirectory != null) {
			                IPath relativePath = workingDirectory.makeRelativeTo(getProject().getFullPath());
			                try {
								IPath remoteWorkingPath = 
										new Path(remRes.getActiveLocationURI().toURL().getPath()).append(relativePath);
				                IFileStore wd = fConnection.getFileManager().getResource(remoteWorkingPath.toString());
								processBuilder.directory(wd);
							} catch (MalformedURLException e) {
								fLocalLauncher.setErrorMessage(e.getMessage());
								return null;
							}
						}
						Map<String, String> processEnv = processBuilder.environment();
						for (String key : fEnvironment.stringPropertyNames()) {
							processEnv.put(key, fEnvironment.getProperty(key));
						}
						try {
							fRemoteProcess = processBuilder.start();
							return new RemoteProcessAdapter(fRemoteProcess);
						} catch (IOException e) {
							fLocalLauncher.setErrorMessage(e.getMessage());
							return null;
						}
					}
				}
			}
		}
		return fLocalLauncher.execute(commandPath, args, env, workingDirectory, monitor);
	}

	@Override
	public String[] getCommandArgs() {
		return fCommandArgs;
	}

	@Override
	public String getCommandLine() {
		return getCommandLine(fCommandArgs);
	}

	protected String getCommandLine(String[] commandArgs) {
		return getCommandLineQuoted(commandArgs, false);
	}

	@SuppressWarnings("nls")
	private String getCommandLineQuoted(String[] commandArgs, boolean quote) {
		String nl = System.getProperty("line.separator", "\n");
		if (fConnection != null) {
			nl = fConnection.getProperty(IRemoteConnection.LINE_SEPARATOR_PROPERTY);
		}
		StringBuffer buf = new StringBuffer();
		if (commandArgs != null) {
			for (String commandArg : commandArgs) {
				if (quote && (commandArg.contains(" ") || commandArg.contains("\"") || commandArg.contains("\\"))) {
					commandArg = '"' + commandArg.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"") + '"';
				}
				buf.append(commandArg);
				buf.append(' ');
			}
			buf.append(nl);
		}
		return buf.toString();
	}

	@Override
	public Properties getEnvironment() {
		return fEnvironment;
	}

	@Override
	public String getErrorMessage() {
		return fLocalLauncher.getErrorMessage();
	}

	@Override
	public IProject getProject() {
		return fLocalLauncher.getProject();
	}

	/**
	 * Parse array of "ENV=value" pairs to Properties.
	 */
	private void parseEnvironment(String[] env) {
		if (env != null) {
			fEnvironment.clear();
			for (String envStr : env) {
				// Split "ENV=value" and put in Properties
				int pos = envStr.indexOf('=');
				if (pos < 0) {
					pos = envStr.length();
				}
				String key = envStr.substring(0, pos);
				String value = envStr.substring(pos + 1);
				fEnvironment.put(key, value);
			}
		}
	}

	private void printCommandLine(OutputStream os) {
		if (os != null) {
			try {
				os.write(getCommandLineQuoted(getCommandArgs(), true).getBytes());
				os.flush();
			} catch (IOException e) {
				// ignore;
			}
		}
	}

	@Override
	public void setErrorMessage(String error) {
		fLocalLauncher.setErrorMessage(error);
	}

	@Override
	public void setProject(IProject project) {
		fLocalLauncher.setProject(project);
	}

	@Override
	public void showCommand(boolean show) {
		fLocalLauncher.showCommand(show);
		fShowCommand = show;
	}

	@Override
	public int waitAndRead(OutputStream out, OutputStream err) {
		if (fShowCommand) {
			printCommandLine(out);
		}

		if (fRemoteProcess == null) {
			return ILLEGAL_COMMAND;
		}

		RemoteProcessClosure closure = new RemoteProcessClosure(fRemoteProcess, out, err);
		closure.runBlocking();
		return OK;
	}

	@Override
	public int waitAndRead(OutputStream out, OutputStream err, IProgressMonitor monitor) {
		if (fShowCommand) {
			printCommandLine(out);
		}
		if (fRemoteProcess == null) {
			return ILLEGAL_COMMAND;
		}

		RemoteProcessClosure closure = new RemoteProcessClosure(fRemoteProcess, out, err);
		closure.runNonBlocking();
		while (!monitor.isCanceled() && closure.isAlive()) {
			try {
				Thread.sleep(DELAY);
			} catch (InterruptedException ie) {
				// ignore
			}
		}

		int state = OK;

		// Operation canceled by the user, terminate abnormally.
		if (monitor.isCanceled()) {
			closure.terminate();
			state = COMMAND_CANCELED;
			setErrorMessage(Messages.RemoteCommandLauncher_Command_canceled);
		}

		try {
			fRemoteProcess.waitFor();
		} catch (InterruptedException e) {
			// ignore
		}
		return state;
	}
}
