/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;

import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.cdt.utils.spawner.Spawner;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 */
public class MIProcessAdapter implements MIProcess {

	Process fGDBProcess;
	InputStream gdbInputStream;
	private static final int ONE_SECOND = 1000;
	private long commandTimeout;

	public MIProcessAdapter(String[] args, IProgressMonitor monitor) throws IOException {
		this(args, 0, monitor);
	}

	public MIProcessAdapter(String[] args, int launchTimeout, IProgressMonitor monitor) throws IOException {
		fGDBProcess = getGDBProcess(args, launchTimeout, monitor);
		commandTimeout = MIPlugin.getCommandTimeout();
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
		final Process pgdb = createGDBProcess(args);
		Thread syncStartup = new Thread("GDB Start") { //$NON-NLS-1$
			@Override
			public void run() {
				try {
					PushbackInputStream pb = new PushbackInputStream(pgdb.getInputStream());
					gdbInputStream = pb;
					pb.unread(pb.read());  // actually read something, then return it
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
			throw new OperationCanceledException();
		} else if (timepass >= launchTimeout) {
			pgdb.destroy();
			String message = MIPlugin.getResourceString("src.GDBDebugger.Error_launch_timeout"); //$NON-NLS-1$
			throw new IOException(message);
		}
		return pgdb;
	}

	/**
	 * Basic process creation hook. Subclasses may override to create the process some other way,
	 * for example by setting the child process's environment.
	 * 
	 * @param args
	 *            the <tt>gdb</tt> command-line
	 * @return the <tt>gdb</tt> process
	 * @throws IOException
	 *             on failure to create the child process
	 * 
	 * @since 7.0
	 */
	protected Process createGDBProcess(String[] args) throws IOException {
		return ProcessFactory.getFactory().exec(args);
	}
	
	@Override
	public boolean canInterrupt(MIInferior inferior) {
		return fGDBProcess instanceof Spawner;
	}

	@Override
	public void interrupt(MIInferior inferior) {
		if (fGDBProcess instanceof Spawner) {
			if (inferior.isRunning()) {
				Spawner gdbSpawner = (Spawner) fGDBProcess;
				if (inferior.isAttachedInferior() && !inferior.isRemoteInferior()) {
					// not all gdb versions forward the interrupt to an attached
					// local inferior, so interrupt the inferior directly
					interruptInferior(inferior);
				}
				else {
					// standard case (gdb launches process) and remote case (gdbserver)
					gdbSpawner.interrupt();
				}
				waitForInterrupt(inferior);
			}
		}
	}

	protected boolean waitForInterrupt(MIInferior inferior) {
	    synchronized (inferior) {
	    	// Allow MI command timeout for the interrupt to propagate.
	    	long maxSec = commandTimeout / ONE_SECOND + 1;
	    	for (int i = 0; inferior.isRunning() && i < maxSec; i++) {
	    		try {
	    			inferior.wait(ONE_SECOND);
	    		} catch (InterruptedException e) {
	    		}
	    	}
	    	return inferior.isRunning();
	    }
    }

	/**
	 * Send an interrupt to the inferior process.
	 * 
	 * @param inferior
	 */
	protected void interruptInferior(MIInferior inferior) {
		if (fGDBProcess instanceof Spawner) {
			Spawner gdbSpawner = (Spawner) fGDBProcess;
			gdbSpawner.raise(inferior.getInferiorPID(), gdbSpawner.INT);
		}
	}
	
	@Override
	public int exitValue() {
		return fGDBProcess.exitValue();
	}

	@Override
	public int waitFor() throws InterruptedException {
		return fGDBProcess.waitFor();
	}

	@Override
	public void destroy() {
    	// We are responsible for closing the streams we have used or else
    	// we will leak pipes.  
    	// Bug 345164
    	try {
    		getErrorStream().close();
		} catch (IOException e) {}
    	try {
    		getInputStream().close();
		} catch (IOException e) {}
    	try {
    		getOutputStream().close();
		} catch (IOException e) {}

		fGDBProcess.destroy();
	}

	@Override
	public InputStream getErrorStream() {
		return fGDBProcess.getErrorStream();
	}

	@Override
	public InputStream getInputStream() {
		return gdbInputStream;
	}

	@Override
	public OutputStream getOutputStream() {
		return fGDBProcess.getOutputStream();
	}

}
