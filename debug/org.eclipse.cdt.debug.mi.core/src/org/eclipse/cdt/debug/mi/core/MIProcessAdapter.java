/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.cdt.utils.spawner.Spawner;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 */
public class MIProcessAdapter implements MIProcess {

	Process fGDBProcess;
	private static final int ONE_SECOND = 1000;

	public MIProcessAdapter(String[] args, IProgressMonitor monitor) throws IOException {
		this(args, 0, monitor);
	}

	public MIProcessAdapter(String[] args, int launchTimeout, IProgressMonitor monitor) throws IOException {
		fGDBProcess = getGDBProcess(args, launchTimeout, monitor);
	}

	/**
	 * Do some basic synchronisation, gdb may take some time to load for
	 * whatever reasons and we need to be able to let the user bailout.
	 * 
	 * @param args
	 * @return Process
	 * @throws IOException
	 */
	protected Process getGDBProcess(String[] args, int launchTimeout, IProgressMonitor monitor) throws IOException {
		final Process pgdb = ProcessFactory.getFactory().exec(args);
		Thread syncStartup = new Thread("GDB Start") { //$NON-NLS-1$
			public void run() {
				try {
					String line;
					InputStream stream = pgdb.getInputStream();
					Reader r = new InputStreamReader(stream);
					BufferedReader reader = new BufferedReader(r);
					while ((line = reader.readLine()) != null) {
						line = line.trim();
						//System.out.println("GDB " + line);
						if (line.endsWith("(gdb)")) { //$NON-NLS-1$
							break;
						}
					}
				} catch (Exception e) {
					// Do nothing, ignore the errors
				}
			}
		};
		syncStartup.start();

		int timepass = 0;
		if (launchTimeout <= 0) {
			// Simulate we are waiting forever.
			launchTimeout = Integer.MAX_VALUE;
		}

		// To respect the IProgressMonitor we can not use wait/notify
		// instead we have to loop and check for the monitor to allow to cancel the thread.
		// The monitor is check every 1 second delay;
		for (timepass = 0; timepass < launchTimeout; timepass += ONE_SECOND) {
			if (syncStartup.isAlive() && !monitor.isCanceled()) {
				try {
					Thread.sleep(ONE_SECOND);
				} catch (InterruptedException e) {
					// ignore
				}
			} else {
				break;
			}
		}
		try {
			syncStartup.interrupt();
			syncStartup.join(ONE_SECOND);
		} catch (InterruptedException e) {
			// ignore
		}
		if (monitor.isCanceled()) {
			pgdb.destroy();
			String message = MIPlugin.getResourceString("src.GDBDebugger.Error_creating_session");//$NON-NLS-1$
			throw new IOException(message);
		} else if (timepass > launchTimeout) {
			pgdb.destroy();
			String message = MIPlugin.getResourceString("src.GDBDebugger.Error_launch_timeout"); //$NON-NLS-1$
			throw new IOException(message);
		}
		return pgdb;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.mi.core.MIProcess#canInterrupt()
	 */
	public boolean canInterrupt(MIInferior inferior) {
		return fGDBProcess instanceof Spawner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.mi.core.MIProcess#interrupt()
	 */
	public void interrupt(MIInferior inferior) {
		if (fGDBProcess instanceof Spawner) {
			Spawner gdbSpawner = (Spawner) fGDBProcess;
			gdbSpawner.interrupt();
			synchronized (inferior) {
				// Allow (5 secs) for the interrupt to propagate.
				for (int i = 0; inferior.isRunning() && i < 5; i++) {
					try {
						inferior.wait(1000);
					} catch (InterruptedException e) {
					}
				}
			}
			// If we are still running try to drop the sig to the PID
			if (inferior.isRunning() && inferior.getInferiorPID() > 0) {
				// lets try something else.
				gdbSpawner.raise(inferior.getInferiorPID(), gdbSpawner.INT);
				synchronized (inferior) {
					for (int i = 0; inferior.isRunning() && i < 5; i++) {
						try {
							inferior.wait(1000);
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#exitValue()
	 */
	public int exitValue() {
		return fGDBProcess.exitValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#waitFor()
	 */
	public int waitFor() throws InterruptedException {
		return fGDBProcess.waitFor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#destroy()
	 */
	public void destroy() {
		fGDBProcess.destroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#getErrorStream()
	 */
	public InputStream getErrorStream() {
		return fGDBProcess.getErrorStream();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#getInputStream()
	 */
	public InputStream getInputStream() {
		return fGDBProcess.getInputStream();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#getOutputStream()
	 */
	public OutputStream getOutputStream() {
		return fGDBProcess.getOutputStream();
	}

}
