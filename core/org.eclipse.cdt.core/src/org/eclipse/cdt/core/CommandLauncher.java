/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.eclipse.cdt.internal.core.Cygwin;
import org.eclipse.cdt.internal.core.ProcessClosure;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.cdt.utils.spawner.EnvironmentReader;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CommandLauncher implements ICommandLauncher {
	public final static int COMMAND_CANCELED = ICommandLauncher.COMMAND_CANCELED;
	public final static int ILLEGAL_COMMAND = ICommandLauncher.ILLEGAL_COMMAND;
	public final static int OK = ICommandLauncher.OK;

	private static final String PATH_ENV = "PATH"; //$NON-NLS-1$

	protected Process fProcess;
	protected boolean fShowCommand;
	protected String[] fCommandArgs;
	private Properties fEnvironment = null;

	protected String fErrorMessage = ""; //$NON-NLS-1$

	private String lineSeparator;
	private IProject fProject;

	/**
	 * The number of milliseconds to pause between polling.
	 */
	protected static final long DELAY = 50L;

	/**
	 * Creates a new launcher Fills in stderr and stdout output to the given
	 * streams. Streams can be set to <code>null</code>, if output not
	 * required
	 */
	public CommandLauncher() {
		fProcess = null;
		fShowCommand = false;
		lineSeparator = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICommandLauncher#showCommand(boolean)
	 */
	@Override
	public void showCommand(boolean show) {
		fShowCommand = show;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICommandLauncher#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		return fErrorMessage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICommandLauncher#setErrorMessage(java.lang.String)
	 */
	@Override
	public void setErrorMessage(String error) {
		fErrorMessage = error;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICommandLauncher#getCommandArgs()
	 */
	@Override
	public String[] getCommandArgs() {
		return fCommandArgs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICommandLauncher#getEnvironment()
	 */
	@Override
	public Properties getEnvironment() {
		if (fEnvironment == null) {
			// for backward compatibility, note that this return may be not accurate
			return EnvironmentReader.getEnvVars();
		}
		return fEnvironment;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICommandLauncher#getCommandLine()
	 */
	@Override
	public String getCommandLine() {
		return getCommandLine(getCommandArgs());
	}

	/**
	 * Constructs a command array that will be passed to the process
	 */
	protected String[] constructCommandArray(String command, String[] commandArgs) {
		String[] args = new String[1 + commandArgs.length];
		args[0] = command;
		System.arraycopy(commandArgs, 0, args, 1, commandArgs.length);
		return args;
	}

	/**
	 * Parse array of "ENV=value" pairs to Properties.
	 */
	private Properties parseEnv(String[] env) {
		Properties envProperties = new Properties();
		for (String envStr : env) {
			// Split "ENV=value" and put in Properties
			int pos = envStr.indexOf('=');
			if (pos < 0)
				pos = envStr.length();
			String key = envStr.substring(0, pos);
			String value = envStr.substring(pos + 1);
			envProperties.put(key, value);
		}
		return envProperties;
	}

	/**
	 * @deprecated
	 * @since 5.1
	 */
	@Deprecated
	public Process execute(IPath commandPath, String[] args, String[] env, IPath changeToDirectory) {
		try {
			return execute(commandPath, args, env, changeToDirectory, null);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}


	/**
	 * @since 5.1
	 * @see org.eclipse.cdt.core.ICommandLauncher#execute(IPath, String[], String[], IPath, IProgressMonitor)
	 */
	@Override
	public Process execute(IPath commandPath, String[] args, String[] env, IPath changeToDirectory, IProgressMonitor monitor) throws CoreException {
		try {
			String command = commandPath.toOSString();
			fCommandArgs = constructCommandArray(command, args);
			fEnvironment = parseEnv(env);

			File file = null;

			if(changeToDirectory != null)
				file = changeToDirectory.toFile();

			if (Platform.getOS().equals(Platform.OS_WIN32)) {
				// Handle cygwin link
				String envPathValue = (String) getEnvironment().get(PATH_ENV);
				IPath location = PathUtil.findProgramLocation(command, envPathValue);
				if(location != null) {
					try {
						fCommandArgs[0] = Cygwin.cygwinToWindowsPath(location.toString(), envPathValue);
					} catch (Exception e) {
						// if no cygwin nothing to worry about
					}
				}
			}

			fProcess = ProcessFactory.getFactory().exec(fCommandArgs, env, file);
			fCommandArgs[0] = command; // to print original command on the console
			fErrorMessage = ""; //$NON-NLS-1$
		} catch (IOException e) {
			setErrorMessage(e.getMessage());
			fProcess = null;
		}
		return fProcess;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICommandLauncher#waitAndRead(java.io.OutputStream, java.io.OutputStream)
	 */
	@Override
	@Deprecated
	public int waitAndRead(OutputStream out, OutputStream err) {
		if (fShowCommand) {
			printCommandLine(out);
		}

		if (fProcess == null) {
			return ILLEGAL_COMMAND;
		}

		ProcessClosure closure = new ProcessClosure(fProcess, out, err);
		closure.runBlocking(); // a blocking call
		return OK;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICommandLauncher#waitAndRead(java.io.OutputStream, java.io.OutputStream, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public int waitAndRead(OutputStream output, OutputStream err, IProgressMonitor monitor) {
		if (fShowCommand) {
			printCommandLine(output);
		}

		if (fProcess == null) {
			return ILLEGAL_COMMAND;
		}

		ProcessClosure closure = new ProcessClosure(fProcess, output, err);
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
			setErrorMessage(CCorePlugin.getResourceString("CommandLauncher.error.commandCanceled")); //$NON-NLS-1$
		}

		try {
			fProcess.waitFor();
		} catch (InterruptedException e) {
			// ignore
		}
		return state;
	}

	protected void printCommandLine(OutputStream os) {
		if (os != null) {
			String cmd = getCommandLine(getCommandArgs());
			try {
				os.write(cmd.getBytes());
				os.flush();
			} catch (IOException e) {
				// ignore;
			}
		}
	}

	protected String getCommandLine(String[] commandArgs) {
		StringBuffer buf = new StringBuffer();
		if (fCommandArgs != null) {
			for (String commandArg : commandArgs) {
				buf.append(commandArg);
				buf.append(' ');
			}
			buf.append(lineSeparator);
		}
		return buf.toString();
	}


	/**
	 * @since 5.1
	 * @see org.eclipse.cdt.core.ICommandLauncher#getProject()
	 */
	@Override
	public IProject getProject() {
		return fProject;
	}

	/**
	 * @since 5.1
	 * @see org.eclipse.cdt.core.ICommandLauncher#setProject(org.eclipse.core.resources.IProject)
	 */
	@Override
	public void setProject(IProject project) {
		fProject = project;
	}

}