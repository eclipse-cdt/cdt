package org.eclipse.cdt.utils.spawner;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;

public class Spawner extends Process {

	private int NOOP = 0;
	private int HUP = 1;
	private int INT = 2;
	private int KILL = 9;
	private int TERM = 15;

	private int pid = -1;
	private int status;
	private int[] channels = new int[3];
	OutputStream out;
	InputStream in;
	InputStream err;

	public Spawner(String command, boolean bNoRedirect) throws IOException {
		StringTokenizer tokenizer = new StringTokenizer(command);
		String[] cmdarray = new String[tokenizer.countTokens()];
		for (int n = 0; tokenizer.hasMoreTokens(); n++)
			cmdarray[n] = tokenizer.nextToken();
		if(bNoRedirect)
			exec_detached(cmdarray, new String[0], ".");
		else
			exec(cmdarray, new String[0], ".");
	}
	/**
	 * Executes the specified command and arguments in a separate process with the
	 * specified environment and working directory.
	 **/
	protected Spawner(String[] cmdarray, String[] envp, File dir) throws IOException {
		String dirpath = ".";
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
		String dirpath = ".";
		if (dir != null)
			dirpath = dir.getAbsolutePath();
		exec(cmdarray, envp, dirpath);
	}

	/**
	 * See java.lang.Process#getInputStream ();
	 **/
	public InputStream getInputStream() {
		return in;
		//return new SpawnerInputStream(channels[1]);
	}

	/**
	 * See java.lang.Process#getOutputStream ();
	 **/
	public OutputStream getOutputStream() {
		return out;
		//return new SpawnerOutputStream(channels[0]);
	}

	/**
	 * See java.lang.Process#getErrorStream ();
	 **/
	public InputStream getErrorStream() {
		return err;
		//return new SpawnerInputStream(channels[2]);
	}

	/**
	 * See java.lang.Process#waitFor ();
	 **/
	public int waitFor() throws InterruptedException {
		status = waitFor(pid);
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
	public int exitValue() {
		if (isRunning()) {
			throw new IllegalThreadStateException("Process not Terminated");
		}
		return status;
	}

	/**
	 * See java.lang.Process#destroy ();
	 **/
	public void destroy() {
		terminate();
		if (isRunning()) {
			kill();
		}
		try {
			waitFor();
		} catch (InterruptedException e) {
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
		pid = exec0(cmdarray, envp, dirpath, channels);
		if (pid == -1) {
			throw new IOException("Exec error");
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
			throw new IOException("Exec error");
		}
	}

	private native int exec0( String[] cmdarray, String[] envp, String dir, int[] chan);
	private native int exec1( String[] cmdarray, String[] envp, String dir);
	private native int raise(int pid, int sig);
	private native int waitFor(int pid);

	static {
		System.loadLibrary("spawner");
	}
}
