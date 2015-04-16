/*******************************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.streams;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalServiceOutputStreamMonitorListener;
import org.eclipse.ui.services.IDisposable;

/**
 * Streams connector implementation.
 */
@SuppressWarnings("restriction")
public abstract class AbstractStreamsConnector extends TerminalConnectorImpl {
	// Reference to the stdin monitor
    private InputStreamMonitor stdInMonitor;
    // Reference to the stdout monitor
    private OutputStreamMonitor stdOutMonitor;
    // Reference to the stderr monitor
    private OutputStreamMonitor stdErrMonitor;

    // Reference to the list of stdout output listeners
    private ITerminalServiceOutputStreamMonitorListener[] stdoutListeners = null;
    // Reference to the list of stderr output listeners
    private ITerminalServiceOutputStreamMonitorListener[] stderrListeners = null;

    /**
     * Set the list of stdout listeners.
     *
     * @param listeners The list of stdout listeners or <code>null</code>.
     */
    protected final void setStdoutListeners(ITerminalServiceOutputStreamMonitorListener[] listeners) {
    	this.stdoutListeners = listeners;
    }

    /**
     * Set the list of stderr listeners.
     *
     * @param listeners The list of stderr listeners or <code>null</code>.
     */
    protected final void setStderrListeners(ITerminalServiceOutputStreamMonitorListener[] listeners) {
    	this.stderrListeners = listeners;
    }

    /**
     * Connect the given streams. The streams connector will wrap each stream
     * with a corresponding terminal stream monitor.
     *
     * @param terminalControl The terminal control. Must not be <code>null</code>.
     * @param stdin The stdin stream or <code>null</code>.
     * @param stdout The stdout stream or <code>null</code>.
     * @param stderr The stderr stream or <code>null</code>.
	 * @param localEcho Local echo on or off.
	 * @param lineSeparator The line separator used by the stream.
     */
    protected void connectStreams(ITerminalControl terminalControl, OutputStream stdin, InputStream stdout, InputStream stderr, boolean localEcho, String lineSeparator) {
    	Assert.isNotNull(terminalControl);

    	// Create the input stream monitor
    	if (stdin != null) {
    		stdInMonitor = createStdInMonitor(terminalControl, stdin, localEcho, lineSeparator);
    		// Register the connector if it implements IDisposable and stdout/stderr are not monitored
    		if (stdout == null && stderr == null && this instanceof IDisposable) stdInMonitor.addDisposable((IDisposable)this);
    		// Start the monitoring
    		stdInMonitor.startMonitoring();
    	}

    	// Create the output stream monitor
    	if (stdout != null) {
    		stdOutMonitor = createStdOutMonitor(terminalControl, stdout, lineSeparator);
    		// Register the connector if it implements IDisposable
    		if (this instanceof IDisposable) stdOutMonitor.addDisposable((IDisposable)this);
    		// Register the listeners
    		if (stdoutListeners != null) {
    			for (ITerminalServiceOutputStreamMonitorListener l : stdoutListeners) {
    				stdOutMonitor.addListener(l);
    			}
    		}
    		// Start the monitoring
    		stdOutMonitor.startMonitoring();
    	}

    	// Create the error stream monitor
    	if (stderr != null) {
    		stdErrMonitor = createStdErrMonitor(terminalControl, stderr, lineSeparator);
    		// Register the connector if it implements IDisposable and stdout is not monitored
    		if (stdout == null && this instanceof IDisposable) stdErrMonitor.addDisposable((IDisposable)this);
    		// Register the listeners
    		if (stderrListeners != null) {
    			for (ITerminalServiceOutputStreamMonitorListener l : stderrListeners) {
    				stdErrMonitor.addListener(l);
    			}
    		}
    		// Start the monitoring
    		stdErrMonitor.startMonitoring();
    	}
    }

    /**
     * Creates an stdin monitor for the given terminal control and stdin stream.
     * Subclasses may override to create a specialized stream monitor.
     *
     * @param terminalControl The terminal control. Must not be <code>null</code>.
     * @param stdin The stdin stream or <code>null</code>.
	 * @param localEcho Local echo on or off.
	 * @param lineSeparator The line separator used by the stream.
	 *
     * @return input stream monitor
     */
    protected InputStreamMonitor createStdInMonitor(ITerminalControl terminalControl, OutputStream stdin, boolean localEcho, String lineSeparator) {
        return new InputStreamMonitor(terminalControl, stdin, localEcho, lineSeparator);
    }

    /**
     * Creates an stdout monitor for the given terminal control and stdout stream.
     * Subclasses may override to create a specialized stream monitor.
     *
     * @param terminalControl The terminal control. Must not be <code>null</code>.
     * @param stdout The stdout stream or <code>null</code>.
	 * @param lineSeparator The line separator used by the stream.
	 *
     * @return output stream monitor
     */
    protected OutputStreamMonitor createStdOutMonitor(ITerminalControl terminalControl, InputStream stdout, String lineSeparator) {
        return new OutputStreamMonitor(terminalControl, stdout, lineSeparator);
    }

    /**
     * Creates an stderr monitor for the given terminal control and stderr stream.
     * Subclasses may override to create a specialized stream monitor.
     *
     * @param terminalControl The terminal control. Must not be <code>null</code>.
     * @param stderr The stderr stream or <code>null</code>.
	 * @param lineSeparator The line separator used by the stream.
	 *
     * @return output stream monitor
     */
    protected OutputStreamMonitor createStdErrMonitor(ITerminalControl terminalControl, InputStream stderr, String lineSeparator) {
        return new OutputStreamMonitor(terminalControl, stderr, lineSeparator);
    }

    /* (non-Javadoc)
     * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#doDisconnect()
     */
    @Override
    protected void doDisconnect() {
    	// Dispose the streams
        if (stdInMonitor != null) { stdInMonitor.dispose(); stdInMonitor = null; }
        if (stdOutMonitor != null) { stdOutMonitor.dispose(); stdOutMonitor = null; }
        if (stdErrMonitor != null) { stdErrMonitor.dispose(); stdErrMonitor = null; }

    	super.doDisconnect();
    }

    /* (non-Javadoc)
     * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#getTerminalToRemoteStream()
     */
	@Override
	public OutputStream getTerminalToRemoteStream() {
		return stdInMonitor;
	}

}
