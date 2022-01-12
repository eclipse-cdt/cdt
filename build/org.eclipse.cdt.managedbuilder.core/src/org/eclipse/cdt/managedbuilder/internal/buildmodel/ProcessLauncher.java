/*******************************************************************************
 * Copyright (c) 2006, 2016 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.ProcessClosure;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This class implements external process launching for internal builder.
 *
 * NOTE: This class is subject to change and discuss,
 * and is currently available in experimental mode only
 */
public class ProcessLauncher {
	public final static int STATE_DONE = 0;
	public final static int STATE_RUNNING = 1;
	public final static int STATE_CANCELED = 2;
	public final static int STATE_ILLEGAL = -1;

	protected String[] cmd;
	protected String[] env;
	protected File cwd;
	protected OutputStream out;
	protected OutputStream err;
	protected IProgressMonitor monitor;
	protected boolean show;
	protected String error;
	protected String lineSeparator;
	protected Process process;
	protected ProcessClosure closure = null;
	protected int state;

	/**
	 * Returns command line as a string array
	 */
	public String[] getCommandArray() {
		return cmd;
	}

	/**
	 * Returns command line in a single string
	 */
	public String getCommandLine() {
		StringBuilder buf = new StringBuilder();
		if (cmd != null) {
			for (int i = 0; i < cmd.length; i++) {
				buf.append(cmd[i]);
				buf.append(' ');
			}
			buf.append(lineSeparator);
		}

		return buf.toString();
	}

	/**
	 * Returns process environment
	 */
	public String[] getEnvironment() {
		return env;
	}

	/**
	 * Returns command working directory
	 */
	public File getWorkingDir() {
		return cwd;
	}

	/**
	 * Returns error message (if any)
	 */
	public String getErrorMessage() {
		return error;
	}

	/**
	 * Returns exit code of a process
	 */
	public int getExitCode() {
		if (process == null || closure.isAlive())
			return 0;
		try {
			return process.waitFor();
		} catch (InterruptedException e) {
			return 0;
		}
	}

	/**
	 * Initializes launcher
	 * @param _cmd Command path
	 * @param args Command arguments
	 * @param _env Environment
	 * @param _cwd Working directory
	 * @param _out Output stream
	 * @param _err Error output stream
	 * @param _monitor Progress monitor
	 * @param _show If true, print command line before launching
	 */
	public ProcessLauncher(IPath _cmd, String[] args, String[] _env, IPath _cwd, OutputStream _out, OutputStream _err,
			IProgressMonitor _monitor, boolean _show) {
		cmd = createCmdArray(_cmd.toOSString(), args);
		env = _env;
		cwd = _cwd.toFile();
		out = _out;
		err = _err;
		monitor = _monitor;
		show = _show;
		error = ""; //$NON-NLS-1$
		lineSeparator = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Launches a process
	 */
	public void launch() {
		try {
			if (show)
				printCommandLine();
			state = STATE_RUNNING;
			process = ProcessFactory.getFactory().exec(cmd, env, cwd);
			closure = new ProcessClosure(process, out, err);
			// Close the input of the process since we will never write to it
			try {
				process.getOutputStream().close();
			} catch (IOException e) {
				// do nothing
			}
			closure.runNonBlocking();
		} catch (IOException e) {
			error = e.getMessage();
			closure = null;
		}
	}

	/**
	 * Returns process state
	 */
	public int queryState() {
		if (state == STATE_RUNNING) {
			if (closure == null)
				state = STATE_ILLEGAL;
			else if (monitor.isCanceled()) {
				closure.terminate();
				error = CCorePlugin.getResourceString("CommandLauncher.error.commandCanceled"); //$NON-NLS-1$
				state = STATE_CANCELED;
			} else if (!closure.isRunning()) {
				state = STATE_DONE;
			}
		}

		return state;
	}

	/**
	 * Creates a string array representing the command that will be passed
	 * to the process
	 */
	protected String[] createCmdArray(String cmdPath, String[] cmdArgs) {
		String[] args = new String[1 + cmdArgs.length];
		args[0] = cmdPath;
		System.arraycopy(cmdArgs, 0, args, 1, cmdArgs.length);

		return args;
	}

	/**
	 * Prints command line
	 */
	protected void printCommandLine() {
		if (out != null) {
			try {
				out.write(getCommandLine().getBytes());
				out.flush();
			} catch (IOException e) {
				// do nothing
			}
		}
	}
}
