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

package org.eclipse.cdt.debug.mi.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.cdt.utils.spawner.Spawner;
import org.eclipse.core.runtime.Preferences;

/**
 */
public class MIProcessAdapter implements MIProcess {

	Process fGDBProcess;

	public MIProcessAdapter(String[] args) throws IOException {
		fGDBProcess = getGDBProcess(args);
	}

	/**
	 * Do some basic synchronisation, gdb may take some time to load for
	 * whatever reasons.
	 * 
	 * @param args
	 * @return Process
	 * @throws IOException
	 */
	protected Process getGDBProcess(String[] args) throws IOException {
		if (MIPlugin.getDefault().isDebugging()) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < args.length; ++i) {
				sb.append(args[i]);
				sb.append(' ');
			}
			MIPlugin.getDefault().debugLog(sb.toString());
		}
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
					// Do nothing
				}
				synchronized (pgdb) {
					pgdb.notifyAll();
				}
			}
		};
		syncStartup.start();

		synchronized (pgdb) {
			MIPlugin miPlugin = MIPlugin.getDefault();
			Preferences prefs = miPlugin.getPluginPreferences();
			int launchTimeout = prefs
					.getInt(IMIConstants.PREF_REQUEST_LAUNCH_TIMEOUT);
			while (syncStartup.isAlive()) {
				try {
					pgdb.wait(launchTimeout);
					break;
				} catch (InterruptedException e) {
				}
			}
		}
		try {
			syncStartup.interrupt();
			syncStartup.join(1000);
		} catch (InterruptedException e) {
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
			int state;
			// Allow (5 secs) for the interrupt to propagate.
			for (int i = 0; inferior.isRunning() && i < 5; i++) {
				try {
					wait(1000);
				} catch (InterruptedException e) {
				}
			}
			// If we are still running try to drop the sig to the PID
			if (inferior.isRunning() && inferior.getInferiorPID() > 0) {
				// lets try something else.
				gdbSpawner.raise(inferior.getInferiorPID(), gdbSpawner.INT);
				for (int i = 0; inferior.isRunning() && i < 5; i++) {
					try {
						wait(1000);
					} catch (InterruptedException e) {
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