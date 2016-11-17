/*******************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.remote.internal.proxy.server.commands;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.remote.proxy.protocol.core.StreamChannel;
import org.eclipse.remote.proxy.protocol.core.exceptions.ProxyException;

/**
 * TODO: Fix hang if command fails...
 *
 */
public class ServerExecCommand extends AbstractServerCommand {

	private final List<String> command;
	private final Map<String, String> env;
	private final boolean redirect;
	private final boolean appendEnv;
	private final String directory;
	private final InputStream stdin;
	private final OutputStream stdout;
	private final OutputStream stderr;
	private final DataOutputStream result;
	private final DataInputStream cmd;
	
	private Process proc;
	
	private class CommandRunner implements Runnable {
		@Override
		public void run() {
			ProcessBuilder builder = new ProcessBuilder(command);
			try {
				if (!appendEnv) {
					builder.environment().clear();
					builder.environment().putAll(env);
				} else {
					for (Map.Entry<String, String> entry : env.entrySet()) {
						String val = builder.environment().get(entry.getKey());
						if (val == null || !val.equals(entry.getValue())) {
							builder.environment().put(entry.getKey(), entry.getValue());
						}
					}
				}
			} catch (UnsupportedOperationException | IllegalArgumentException  e) {
				// Leave environment untouched
			}
			File dir = new File(directory);
			if (dir.exists() && dir.isAbsolute()) {
				builder.directory(dir);
			}
			builder.redirectErrorStream(redirect);
			try {
				int exit = 0;
				try {
					proc = builder.start();
					Forwarder stdoutFwd = startForwarder("stdout", proc.getInputStream(), stdout); //$NON-NLS-1$
					Forwarder stderrFwd = null;
					if (!redirect) {
						stderrFwd = startForwarder("stderr", proc.getErrorStream(), stderr); //$NON-NLS-1$
					}
					startForwarder("stdin", stdin, proc.getOutputStream()); //$NON-NLS-1$
					new Thread(new ProcMonitor(), "process monitor").start(); //$NON-NLS-1$
					System.err.println("wait for process");
					exit = proc.waitFor();
					System.err.println("wait for process close in");
//					stdoutFwd.terminate();
//					if (!redirect) {
//						stderrFwd.terminate();
//					}
					System.err.println("wait for readers");
					/*
					 * After the process has finished, wait for the stdout and stderr forwarders to finish to
					 * ensure that all output is flushed.
					 */
//					stdoutFwd.waitFor();
//					System.err.println("wait for process finished out");
//					if (stderrFwd != null) {
//						stderrFwd.waitFor();
//					}
					System.err.println("wait for readers done");
				} catch (IOException e) {
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stderr));
					try {
						writer.write(e.getMessage());
						writer.flush();
					} catch (IOException e1) {
						// Things look pretty hopeless
					}
					exit = -1;
				}
				try {
					result.writeInt(exit);
					result.flush();
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
				cmd.readByte();
				if (proc.isAlive()) {
					proc.destroyForcibly();
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
		
		private volatile boolean running = true;
		private boolean terminated = false;
		
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
				while (true) {
//					if (in.available() == 0) {
//						System.err.println("avail=0");
//						/* Avoid spinning if no data */
//						lock.lock();
//						try {
//							cond.await(100, TimeUnit.MILLISECONDS);
//						} catch (InterruptedException e) {
//						} finally {
//							lock.unlock();
//						}
//						continue;
//					}
					n = in.read(buf);
					if (n > 0) {
						out.write(buf, 0, n);
						out.flush();
					}
					if (n==0) System.err.println("forwarder n=0");
					if (n < 0) break;
				}
			} catch (IOException e) {
				// Finish
				System.err.println("forwarder "+e.getMessage());
			}
			System.err.println("forwarder closing name="+name);

			try {
				out.close();
			} catch (IOException e) {
				// Best effort
			}
			lock.lock();
			terminated = true;
			try {
				cond.signalAll();
			} finally {
				lock.unlock();
			}
		}
		
		public void terminate() {
			running = false;
		}
		
		public synchronized void waitFor() {
			lock.lock();
			try {
				if (!terminated) {
					try {
						cond.await();
					} catch (InterruptedException e) {
					}
				}
			} finally {
				lock.unlock();
			}
		}
	}

	public ServerExecCommand(List<String> command, Map<String, String> env, String directory, boolean redirect, boolean appendEnv, StreamChannel chanA, StreamChannel chanB, StreamChannel chanC) {
		this.command = command;
		this.env = env;
		this.directory = directory;
		this.redirect = redirect;
		this.appendEnv = appendEnv;
		this.stdin = chanA.getInputStream();
		this.stdout = chanA.getOutputStream();
		this.stderr = chanB.getOutputStream();
		
		this.result = new DataOutputStream(chanC.getOutputStream());
		this.cmd = new DataInputStream(chanC.getInputStream());
	}

	public void exec() throws ProxyException {
		new Thread(new CommandRunner(), command.get(0)).start();
	}
	
	private Forwarder startForwarder(String name, InputStream in, OutputStream out) {
		Forwarder forwarder = new Forwarder(name, in, out);
		Thread thread = new Thread(forwarder, command.get(0) + " " + name); //$NON-NLS-1$
		thread.start();
		return forwarder;
	}
}
