/*******************************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.process;

import org.eclipse.core.runtime.Assert;


/**
 * Process monitor implementation.
 */
public class ProcessMonitor {
	// Reference to the parent process connector
	private final ProcessConnector processConnector;
	// Reference to the monitored process
	private final Process process;
	// Reference to the monitor thread
	private Thread thread;
	// Flag to mark the monitor disposed
	private boolean disposed;


    /**
     * Constructor.
     *
     * @param processConnector The parent process connector. Must not be <code>null</code>.
     */
    public ProcessMonitor(ProcessConnector processConnector) {
        super();

        Assert.isNotNull(processConnector);
		this.processConnector = processConnector;

		// Query the monitored process for easier access
		this.process = processConnector.getProcess();
    }

    /**
     * Dispose the process monitor.
     */
	public void dispose() {
    	// Set the disposed status
    	disposed = true;
    	// Not initialized -> return immediately
    	if (thread == null) return;

    	// Copy the reference
    	final Thread oldThread = thread;
    	// Unlink the monitor from the thread
    	thread = null;
    	// And interrupt the writer thread
    	oldThread.interrupt();
    }

    /**
     * Starts the terminal output stream monitor.
     */
    public void startMonitoring() {
    	// If already initialized -> return immediately
    	if (thread != null) return;

    	// Create a new runnable which is constantly reading from the stream
    	Runnable runnable = new Runnable() {
    		@Override
			public void run() {
    			monitorProcess();
    		}
    	};

    	// Create the monitor thread
    	thread = new Thread(runnable, "Terminal Process Monitor Thread"); //$NON-NLS-1$

    	// Configure the monitor thread
        thread.setDaemon(true);

        // Start the processing
        thread.start();
    }

    /**
     * Monitors the associated system process, waiting for it to terminate,
     * and notifies the associated process monitor's.
     */
    @SuppressWarnings("restriction")
	public void monitorProcess() {
    	// If already disposed -> return immediately
    	if (disposed) return;

    	try {
    		// Wait for the monitored process to terminate
    		process.waitFor();
    	} catch (InterruptedException ie) {
    		// clear interrupted state
    		Thread.interrupted();
    	} finally {
    		// Dispose the parent process connector
    		if (!disposed)
    			processConnector.disconnect();
    	}
    }
}
