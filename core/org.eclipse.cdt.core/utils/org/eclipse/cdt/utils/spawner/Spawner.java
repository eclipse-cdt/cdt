/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.spawner;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.CCorePlugin;

public class Spawner extends Process {

	public int NOOP = 0;
	public int HUP = 1;
	public int INT = 2;
	public int KILL = 9;
	public int TERM = 15;

	int pid = 0;
	int status;
	int[] channels = new int[3];
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
	public InputStream getInputStream() {
		return in;
	}

	/**
	 * See java.lang.Process#getOutputStream ();
	 **/
	public OutputStream getOutputStream() {
		return out;
	}

	/**
	 * See java.lang.Process#getErrorStream ();
	 **/
	public InputStream getErrorStream() {
		return err;
	}

	/**
	 * See java.lang.Process#waitFor ();
	 **/
	public synchronized int waitFor() throws InterruptedException {
		while (!isDone) {
			wait();
		}
		try {
			((SpawnerInputStream)getErrorStream()).close();
			((SpawnerInputStream)getInputStream()).close();
			((SpawnerOutputStream)getOutputStream()).close();
		} catch (IOException e) {
		}
		return status;
	}

	/**
	 * See java.lang.Process#exitValue ();
	 **/
	public synchronized int exitValue() {
		if (!isDone) {
			throw new IllegalThreadStateException("Process not Terminated"); //$NON-NLS-1$
		}
		return status;
	}

	/**
	 * See java.lang.Process#destroy ();
	 **/
	public synchronized void destroy() {
		// Sends the TERM
		terminate();
		// Close the streams on this side.
		try {
			((SpawnerInputStream)getErrorStream()).close();
			((SpawnerInputStream)getInputStream()).close();
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
	 * Our extensions.
	 **/
	public int interrupt() {
		return raise(pid, INT);
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
			throw new IOException("Exec error:" + reaper.getErrorMessage()); //$NON-NLS-1$
		}
		in = new SpawnerInputStream(channels[1]);
		err = new SpawnerInputStream(channels[2]);
		out = new SpawnerOutputStream(channels[0]);
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

	native int exec0( String[] cmdarray, String[] envp, String dir, int[] chan) throws IOException;
	native int exec1( String[] cmdarray, String[] envp, String dir) throws IOException;
	public native int raise(int pid, int sig);
	native int waitFor(int pid);

	static {
		System.loadLibrary("spawner"); //$NON-NLS-1$
	}

	// Spawn a thread to handle the forking and waiting
	// We do it this way because on linux the SIGCHLD is
	// send to the one thread.  So do the forking and
	// the wait in the same thread.
	class Reaper extends Thread {
		String[] cmdarray;
		String[] envp;
		String dirpath;
		String errMesg;

		public Reaper(String[] array, String[] env, String dir) {
			super("Spawner Reaper"); //$NON-NLS-1$
			cmdarray = array;
			envp = env;
			dirpath = dir;
			errMesg = new String(CCorePlugin.getResourceString("Util.error.cannotRun") + cmdarray[0]); //$NON-NLS-1$
		}

		public void run() {
			try {
				pid = exec0(cmdarray, envp, dirpath, channels);
			} catch (Exception e) {
				pid = -1;
				errMesg = e.getMessage();
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
			return errMesg;
		}
	}
}
