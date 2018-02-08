/*******************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.remote.internal.proxy.server.core.commands;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.remote.proxy.protocol.core.Protocol;
import org.eclipse.remote.proxy.protocol.core.StreamChannel;
import org.eclipse.remote.proxy.protocol.core.exceptions.ProxyException;

public abstract class AbstractServerExecCommand extends AbstractServerCommand {
	
	private class CommandRunner implements Runnable {
		@Override
		public void run() {
			try {
				int exit = 0;
				try {
					proc = doRun();
					Forwarder stdoutFwd = startForwarder("stdout", proc.getInputStream(), stdoutChan); //$NON-NLS-1$
					Forwarder stderrFwd = null;
					if (!redirect) {
						stderrFwd = startForwarder("stderr", proc.getErrorStream(), stderrChan); //$NON-NLS-1$
					}
					startForwarder("stdin", stdinChan, proc.getOutputStream()); //$NON-NLS-1$
					new Thread(new ProcMonitor(), "process monitor").start(); //$NON-NLS-1$
					exit = proc.waitFor();
					/*
					 * After the process has finished, wait for the stdout and stderr forwarders to finish to
					 * ensure that all output is flushed.
					 */
					stdoutFwd.waitFor();
					if (stderrFwd != null) {
						stderrFwd.waitFor();
					}
				} catch (IOException e) {
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stderrChan));
					try {
						writer.write(e.getMessage());
						writer.flush();
					} catch (IOException e1) {
						// Things look pretty hopeless
					}
					exit = -1;
				}
				try {
					resultStream.writeInt(exit);
					resultStream.flush();
				} catch (IOException e) {
					// We're finished anyway
				}
			} catch (InterruptedException e) {
				// Ignore?
			}
		}
	}
	
	private class ProcMonitor implements Runnable {
		@Override
		public void run() {
			try {
				switch (cmdStream.readByte()) {
				case Protocol.CONTROL_KILL:
					doKill(proc);
					break;
				case Protocol.CONTROL_SETTERMINALSIZE:
					int cols = cmdStream.readInt();
					int rows = cmdStream.readInt();
					cmdStream.readInt(); // pixel dimensions not supported
					cmdStream.readInt(); // pixel dimensions not supported
					doSetTerminalSize(proc, cols, rows);
					break;
				}
			} catch (IOException e) {
				// Finish
			}
		}
	}
	
	private class Forwarder implements Runnable {
		private final InputStream in;
		private final OutputStream out;
		private final String name;
		
		private boolean running = true;
		
        private final Lock lock = new ReentrantLock();
        private final Condition cond = lock.newCondition();
		
		public Forwarder(String name, InputStream in, OutputStream out) {
			this.name = name;
			this.in = new BufferedInputStream(in);
			this.out = new BufferedOutputStream(out);
		}

		@Override
		public void run() {
			byte[] buf = new byte[8192];
			int n;
			try {
				while (running) {
					n = in.read(buf);
					if (n > 0) {
						out.write(buf, 0, n);
						out.flush();
					}
					if (n < 0) break;
				}
			} catch (IOException e) {
				// Finish
			}

			lock.lock();
			try {
				running = false;
				try {
					out.close();
				} catch (IOException e) {
					// Best effort
				}
				cond.signalAll();
			} finally {
				lock.unlock();
			}
		}
		
		public String getName() {
			return name;
		}
		
		public synchronized void waitFor() {
			lock.lock();
			try {
				while (running) {
					try {
						cond.await();
					} catch (InterruptedException e) {
						// Check terminated flag
					}
				}
			} finally {
				lock.unlock();
			}
		}
	}
	
	private final List<String> command;
	private final Map<String, String> env;
	private final boolean redirect;
	private final boolean appendEnv;
	private final String directory;
	
	private final InputStream stdinChan;
	private final OutputStream stdoutChan;
	private final OutputStream stderrChan;
	
	private final DataInputStream cmdStream;
	private final DataOutputStream resultStream;

	private Process proc;

	public AbstractServerExecCommand(List<String> command, Map<String, String> env, String directory, boolean redirect, boolean appendEnv, StreamChannel cmdChan, StreamChannel ioChan, StreamChannel errChan) {
		this.command = command;
		this.env = env;
		this.directory = directory;
		this.redirect = redirect;
		this.appendEnv = appendEnv;
		
		this.stdinChan = ioChan.getInputStream();
		this.stdoutChan = ioChan.getOutputStream();
		
		this.stderrChan = errChan != null ? errChan.getOutputStream() : this.stdoutChan;
		
		this.resultStream = new DataOutputStream(cmdChan.getOutputStream());
		this.cmdStream = new DataInputStream(cmdChan.getInputStream());
	}

	protected abstract Process doRun() throws IOException;
	
	protected abstract void doKill(Process proc);
	
	protected abstract void doSetTerminalSize(Process proc, int col, int rows);

	protected List<String> getCommand() {
		return command;
	}
	
	protected Map<String,String> getEnv() {
		return env;
	}
	
	protected boolean isRedirect() {
		return redirect;
	}
	
	protected boolean isAppendEnv() {
		return appendEnv;
	}
	
	protected String getDirectory() {
		return directory;
	}
	
	public void exec() throws ProxyException {
		new Thread(new CommandRunner()).start();
	}
	
	private Forwarder startForwarder(String name, InputStream in, OutputStream out) {
		Forwarder forwarder = new Forwarder(name, in, out);
		Thread thread = new Thread(forwarder, forwarder.getName());
		thread.start();
		return forwarder;
	}
}
