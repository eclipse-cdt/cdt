/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems, Inc. - bug 248071
 *******************************************************************************/
package org.eclipse.cdt.utils.spawner;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;

public class Spawner extends Process {

	public int NOOP = 0;
	public int HUP = 1;
	public int KILL = 9;
	public int TERM = 15;

	/**
	 * On Windows, what this does is far from easy to explain. 
	 * Some of the logic is in the JNI code, some in the spawner.exe code.
	 * 
	 * <ul>
	 * <li>If the process this is being raised against was launched by us (the Spawner)
	 *    <ul>
	 *    <li>If the process is a cygwin program (has the cygwin1.dll loaded), then issue a 'kill -SIGINT'. If
	 *    the 'kill' utility isn't available, send the process a CTRL-C
	 *    <li>If the process is <i>not</i> a cygwin program, send the process a CTRL-C
	 *    </ul>
	 * <li>If the process this is being raised against was <i>not</i> launched by us, use 
	 * DebugBreakProcess to interrupt it (sending a CTRL-C is easy only if we share a console 
	 * with the target process) 
	 * </ul>
	 * 
	 * On non-Windows, raising this just raises a POSIX SIGINT  
	 * 
	 */
	public int INT = 2;

	/**
	 * A fabricated signal number for use on Windows only. Tells the starter program to send a CTRL-C
	 * regardless of whether the process is a Cygwin one or not.
	 * 
	 * @since 5.2
	 */
	public int CTRLC = 1000;  // arbitrary high number to avoid collision

	int pid = 0;
	int status;
	int[] fChannels = new int[3];
	boolean isDone;
	OutputStream out;
	InputStream in;
	InputStream err;

	public Spawner(String command, boolean bNoRedirect) throws IOException {
		StringTokenizer tokenizer = new StringTokenizer(command);
		String[] cmdarray = new String[tokenizer.countTokens()];
		for (int n = 0; tokenizer.hasMoreTokens(); n++)
			cmdarray[n] = tokenizer.nextToken();
		if (bNoRedirect)
			exec_detached(cmdarray, new String[0], "."); //$NON-NLS-1$
		else
			exec(cmdarray, new String[0], "."); //$NON-NLS-1$
	}
	/**
	 * Executes the specified command and arguments in a separate process with the
	 * specified environment and working directory.
	 **/
	protected Spawner(String[] cmdarray, String[] envp, File dir) throws IOException {
		String dirpath = "."; //$NON-NLS-1$
		if (dir != null)
			dirpath = dir.getAbsolutePath();
		exec(cmdarray, envp, dirpath);
	}

	protected Spawner(String[] cmdarray, String[] envp, File dir, PTY pty) throws IOException {
		String dirpath = "."; //$NON-NLS-1$
		if (dir != null)
			dirpath = dir.getAbsolutePath();
		exec_pty(cmdarray, envp, dirpath, pty);
	}
	/**
	 * Executes the specified string command in a separate process.
	 **/
	protected Spawner(String command) throws IOException {
		this(command, null);
	}

	/**
	 * Executes the specified command and arguments in a separate process.
	 **/
	protected Spawner(String[] cmdarray) throws IOException {
		this(cmdarray, null);
	}

	/**
	 * Executes the specified command and arguments in a separate process with the
	 * specified environment.
	 **/
	protected Spawner(String[] cmdarray, String[] envp) throws IOException {
		this(cmdarray, envp, null);
	}

	/**
	 * Executes the specified string command in a separate process with the specified
	 * environment.
	 **/
	protected Spawner(String cmd, String[] envp) throws IOException {
		this(cmd, envp, null);
	}

	/**
	 * Executes the specified string command in a separate process with the specified
	 * environment and working directory.
	 **/
	protected Spawner(String command, String[] envp, File dir) throws IOException {
		StringTokenizer tokenizer = new StringTokenizer(command);
		String[] cmdarray = new String[tokenizer.countTokens()];
		for (int n = 0; tokenizer.hasMoreTokens(); n++)
			cmdarray[n] = tokenizer.nextToken();
		String dirpath = "."; //$NON-NLS-1$
		if (dir != null)
			dirpath = dir.getAbsolutePath();
		exec(cmdarray, envp, dirpath);
	}

	/**
	 * See java.lang.Process#getInputStream ();
	 **/
	@Override
	public InputStream getInputStream() {
		if(null == in)
			in = new SpawnerInputStream(fChannels[1]);
		return in;
	}

	/**
	 * See java.lang.Process#getOutputStream ();
	 **/
	@Override
	public OutputStream getOutputStream() {
		if(null == out)
			out = new SpawnerOutputStream(fChannels[0]);
		return out;
	}

	/**
	 * See java.lang.Process#getErrorStream ();
	 **/
	@Override
	public InputStream getErrorStream() {
		if(null == err)
			err = new SpawnerInputStream(fChannels[2]);
		return err;
	}

	/**
	 * See java.lang.Process#waitFor ();
	 **/
	@Override
	public synchronized int waitFor() throws InterruptedException {
		while (!isDone) {
			wait();
		}
		try {
			if(null == err)
				((SpawnerInputStream)getErrorStream()).close();
			if(null == in)
				((SpawnerInputStream)getInputStream()).close();
			if(null == out)
				((SpawnerOutputStream)getOutputStream()).close();
			
		} catch (IOException e) {
		}
		return status;
	}

	/**
	 * See java.lang.Process#exitValue ();
	 **/
	@Override
	public synchronized int exitValue() {
		if (!isDone) {
			throw new IllegalThreadStateException("Process not Terminated"); //$NON-NLS-1$
		}
		return status;
	}

	/**
	 * See java.lang.Process#destroy ();
	 **/
	@Override
	public synchronized void destroy() {
		// Sends the TERM
		terminate();
		// Close the streams on this side.
		try {
			if(null == err)
				((SpawnerInputStream)getErrorStream()).close();
			if(null == in)
				((SpawnerInputStream)getInputStream()).close();
			if(null == out)
				((SpawnerOutputStream)getOutputStream()).close();
		} catch (IOException e) {
		}
		// Grace before using the heavy gone.
		if (!isDone) {
			try {
				wait(1000);
			} catch (InterruptedException e) {
			}
		}
		if (!isDone) {
			kill();
		}
	}

	/**
	 * On Windows, interrupt the spawned program by using Cygwin's utility 'kill -SIGINT' if it's a Cgywin
	 * program, otherwise send it a CTRL-C. If Cygwin's 'kill' command is not available, send a CTRL-C. On
	 * linux, interrupt it by raising a SIGINT.
	 */
	public int interrupt() {
		return raise(pid, INT);
	}

	/**
	 * On Windows, interrupt the spawned program by send it a CTRL-C (even if it's a Cygwin program). On
	 * linux, interrupt it by raising a SIGINT. 
	 * 
	 * @since 5.2
	 */
	public int interruptCTRLC() {
        if (Platform.getOS().equals(Platform.OS_WIN32)) {
        	return raise(pid, CTRLC);
        }
        else {
        	return interrupt();
        }
	}

	public int hangup() {
		return raise(pid, HUP);
	}

	public int kill() {
		return raise(pid, KILL);
	}

	public int terminate() {
		return raise(pid, TERM);
	}

	public boolean isRunning() {
		return (raise(pid, NOOP) == 0);
	}

	private void exec(String[] cmdarray, String[] envp, String dirpath) throws IOException {
		String command = cmdarray[0];
		SecurityManager s = System.getSecurityManager();
		if (s != null)
			s.checkExec(command);
		if (envp == null)
			envp = new String[0];

		Reaper reaper = new Reaper(cmdarray, envp, dirpath);
		reaper.setDaemon(true);
		reaper.start();

		// Wait until the subprocess is started or error.
		synchronized (this) {
			while (pid == 0) {
				try {
					wait();
				} catch (InterruptedException e) {
				}
			}
		}

		// Check for errors.
		if (pid == -1) {
			throw new IOException(reaper.getErrorMessage());
		}
	}

	private void exec_pty(String[] cmdarray, String[] envp, String dirpath, PTY pty) throws IOException {
		String command = cmdarray[0];
		SecurityManager s = System.getSecurityManager();
		if (s != null)
			s.checkExec(command);
		if (envp == null)
			envp = new String[0];

		final String slaveName = pty.getSlaveName();
		final int masterFD = pty.getMasterFD().getFD();
		final boolean console = pty.isConsole();
		//int fdm = pty.get
		Reaper reaper = new Reaper(cmdarray, envp, dirpath) {
			/* (non-Javadoc)
			 * @see org.eclipse.cdt.utils.spawner.Spawner.Reaper#execute(java.lang.String[], java.lang.String[], java.lang.String, int[])
			 */
			@Override
			int execute(String[] cmd, String[] env, String dir, int[] channels) throws IOException {
				return exec2(cmd, env, dir, channels, slaveName, masterFD, console);
			}
		};
		reaper.setDaemon(true);
		reaper.start();

		// Wait until the subprocess is started or error.
		synchronized (this) {
			while (pid == 0) {
				try {
					wait();
				} catch (InterruptedException e) {
				}
			}
		}

		// Check for errors.
		if (pid == -1) {
			throw new IOException("Exec_tty error:" + reaper.getErrorMessage()); //$NON-NLS-1$
		}
	}

	public void exec_detached(String[] cmdarray, String[] envp, String dirpath) throws IOException {
		String command = cmdarray[0];
		SecurityManager s = System.getSecurityManager();
		if (s != null)
			s.checkExec(command);

		if (envp == null)
			envp = new String[0];
		pid = exec1(cmdarray, envp, dirpath);
		if (pid == -1) {
			throw new IOException("Exec error"); //$NON-NLS-1$
		}
	}

	/**
	 * Native method use in normal exec() calls. 
	 */
	native int exec0( String[] cmdarray, String[] envp, String dir, int[] chan) throws IOException;

	/**
	 * Native method use in no redirect meaning to streams will created. 
	 */
	native int exec1( String[] cmdarray, String[] envp, String dir) throws IOException;

	/**
	 * Native method when executing with a terminal emulation. 
	 */
	native int exec2( String[] cmdarray, String[] envp, String dir, int[] chan, String slaveName, int masterFD, boolean console) throws IOException;

	/**
	 * Native method to drop a signal on the process with pid.
	 */
	public native int raise(int processID, int sig);

	/*
	 * Native method to wait(3) for process to terminate.
	 */
	native int waitFor(int processID);

	static {
		try {
			System.loadLibrary("spawner"); //$NON-NLS-1$
		} catch (SecurityException e) {
			CCorePlugin.log(e);
		} catch (UnsatisfiedLinkError e) {
			CCorePlugin.log(e);
		}
	}

	// Spawn a thread to handle the forking and waiting
	// We do it this way because on linux the SIGCHLD is
	// send to the one thread.  So do the forking and
	// the wait in the same thread.
	class Reaper extends Thread {
		String[] fCmdarray;
		String[] fEnvp;
		String fDirpath;
		volatile Throwable fException;

		public Reaper(String[] array, String[] env, String dir) {
			super("Spawner Reaper"); //$NON-NLS-1$
			fCmdarray = array;
			fEnvp = env;
			fDirpath = dir;
			fException = null;
		}

		int execute(String[] cmdarray, String[] envp, String dir, int[] channels) throws IOException {
			return exec0(cmdarray, envp, dir, channels);
		}

		@Override
		public void run() {
			try {
				pid = execute(fCmdarray, fEnvp, fDirpath, fChannels);
			} catch (Exception e) {
				pid = -1;
				fException= e;
			}

			// Tell spawner that the process started.
			synchronized (Spawner.this) {
				Spawner.this.notifyAll();
			}

			if (pid != -1) {
				// Sync with spawner and notify when done.
				status = waitFor(pid);
				synchronized (Spawner.this) {
					isDone = true;
					Spawner.this.notifyAll();
				}
			}
		}

		public String getErrorMessage() {
			final String reason= fException != null ? fException.getMessage() : "Unknown reason"; //$NON-NLS-1$
			return NLS.bind(CCorePlugin.getResourceString("Util.error.cannotRun"), fCmdarray[0], reason); //$NON-NLS-1$
		}
	}
}
