/*******************************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.streams;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.core.terminals.interfaces.ITerminalServiceOutputStreamMonitorListener;
import org.eclipse.tcf.te.core.terminals.interfaces.constants.ILineSeparatorConstants;
import org.eclipse.tcf.te.ui.terminals.activator.UIPlugin;
import org.eclipse.tcf.te.ui.terminals.interfaces.tracing.ITraceIds;
import org.eclipse.tcf.te.ui.terminals.nls.Messages;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.ui.services.IDisposable;

/**
 * Output stream monitor implementation.
 * <p>
 * <b>Note:</b> The output is going <i>to</i> the terminal. Therefore, the output
 * stream monitor is attached to the stdout and/or stderr stream of the monitored
 * (remote) process.
 */
@SuppressWarnings("restriction")
public class OutputStreamMonitor implements IDisposable {
	// The default buffer size to use
    private static final int BUFFER_SIZE = 8192;

    // Reference to the parent terminal control
    private final ITerminalControl terminalControl;

	// Reference to the monitored (input) stream
    private final InputStream stream;

    // The line separator used by the monitored (input) stream
    private final String lineSeparator;

    // Reference to the thread reading the stream
    private Thread thread;

    // Flag to mark the monitor disposed. When disposed,
    // no further data is read from the monitored stream.
    private boolean disposed;

    // A list of object to dispose if this monitor is disposed
    private final List<IDisposable> disposables = new ArrayList<IDisposable>();

	// The list of registered listener
	private final ListenerList listeners;

    /**
     * Constructor.
     *
     * @param terminalControl The parent terminal control. Must not be <code>null</code>.
     * @param stream The stream. Must not be <code>null</code>.
	 * @param lineSeparator The line separator used by the stream.
     */
	public OutputStreamMonitor(ITerminalControl terminalControl, InputStream stream, String lineSeparator) {
    	super();

    	Assert.isNotNull(terminalControl);
    	this.terminalControl = terminalControl;
    	Assert.isNotNull(stream);
        this.stream = new BufferedInputStream(stream, BUFFER_SIZE);

        this.lineSeparator = lineSeparator;

        this.listeners = new ListenerList();
    }

	/**
	 * Register a streams data receiver listener.
	 *
	 * @param listener The listener. Must not be <code>null</code>.
	 */
	public final void addListener(ITerminalServiceOutputStreamMonitorListener listener) {
		Assert.isNotNull(listener);
		listeners.add(listener);
	}

	/**
	 * Unregister a streams data receiver listener.
	 *
	 * @param listener The listener. Must not be <code>null</code>.
	 */
	public final void removeListener(ITerminalServiceOutputStreamMonitorListener listener) {
		Assert.isNotNull(listener);
		listeners.remove(listener);
	}

	/**
	 * Adds the given disposable object to the list. The method will do nothing
	 * if either the disposable object is already part of the list or the monitor
	 * is disposed.
	 *
	 * @param disposable The disposable object. Must not be <code>null</code>.
	 */
	public final void addDisposable(IDisposable disposable) {
		Assert.isNotNull(disposable);
		if (!disposed && !disposables.contains(disposable)) disposables.add(disposable);
	}

	/**
	 * Removes the disposable object from the list.
	 *
	 * @param disposable The disposable object. Must not be <code>null</code>.
	 */
	public final void removeDisposable(IDisposable disposable) {
		Assert.isNotNull(disposable);
		disposables.remove(disposable);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.services.IDisposable#dispose()
	 */
    @Override
	public void dispose() {
		// If already disposed --> return immediately
		if (disposed) return;

		// Mark the monitor disposed
    	disposed = true;

        // Close the stream (ignore exceptions on close)
        try { stream.close(); } catch (IOException e) { /* ignored on purpose */ }

        // Dispose all registered disposable objects
        for (IDisposable disposable : disposables) disposable.dispose();
        // Clear the list
        disposables.clear();
    }

    /**
     * Starts the terminal output stream monitor.
     */
    protected void startMonitoring() {
    	// If already initialized -> return immediately
    	if (thread != null) return;

    	// Create a new runnable which is constantly reading from the stream
    	Runnable runnable = new Runnable() {
    		@Override
			public void run() {
    			readStream();
    		}
    	};

    	// Create the reader thread
    	thread = new Thread(runnable, "Terminal Output Stream Monitor Thread"); //$NON-NLS-1$

    	// Configure the reader thread
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);

        // Start the processing
        thread.start();
    }

    /**
     * Returns the terminal control that this stream monitor is associated with.
     */
    protected ITerminalControl getTerminalControl() {
        return terminalControl;
    }

    /**
     * Reads from the output stream and write the read content
     * to the terminal control output stream.
     */
    void readStream() {
    	// Creates the read buffer
        byte[] readBuffer = new byte[BUFFER_SIZE];

        // We need to maintain UI responsiveness but still stream the content
        // to the terminal control fast. Put the thread to a short sleep each second.
        long sleepMarker = System.currentTimeMillis();

        // Read from the stream until EOS is reached or the
        // monitor is marked disposed.
        int read = 0;
        while (read >= 0 && !disposed) {
            try {
				// Read from the stream
				read = stream.read(readBuffer);
				// If some data has been read, append to the terminal
				// control output stream
				if (read > 0) {
					// Allow for post processing the read content before appending
                    byte[] processedReadBuffer = onContentReadFromStream(readBuffer, read);
				    if (processedReadBuffer != readBuffer) {
				        read = processedReadBuffer.length;
				    }
					terminalControl.getRemoteToTerminalOutputStream().write(processedReadBuffer, 0, read);
				}
            } catch (IOException e) {
            	// IOException received. If this is happening when already disposed -> ignore
				if (!disposed) {
					IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
												NLS.bind(Messages.OutputStreamMonitor_error_readingFromStream, e.getLocalizedMessage()), e);
					UIPlugin.getDefault().getLog().log(status);
				}
                break;
            } catch (NullPointerException e) {
				// killing the stream monitor while reading can cause an NPE
				// when reading from the stream
				if (!disposed && thread != null) {
					IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
												NLS.bind(Messages.OutputStreamMonitor_error_readingFromStream, e.getLocalizedMessage()), e);
					UIPlugin.getDefault().getLog().log(status);
				}
				break;
            }

            // See above -> Thread will go to sleep each second
            if (System.currentTimeMillis() - sleepMarker > 1000) {
            	sleepMarker = System.currentTimeMillis();
                try { Thread.sleep(1); } catch (InterruptedException e) { /* ignored on purpose */ }
            }
        }

        // Dispose ourself
        dispose();
    }

    /**
     * Allow for processing of data from byte stream after it is read from
     * client but before it is appended to the terminal. If the returned byte
     * array is different than the one that was passed in with the byteBuffer
     * argument, then the bytesRead value will be ignored and the full
     * returned array will be written out.
     *
     * @param byteBuffer The byte stream. Must not be <code>null</code>.
     * @param bytesRead The number of bytes that were read into the read buffer.
     * @return The processed byte stream.
     *
     */
    protected byte[] onContentReadFromStream(byte[] byteBuffer, int bytesRead) {
    	Assert.isNotNull(byteBuffer);

    	// If tracing is enabled, print out the decimal byte values read
    	if (UIPlugin.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_OUTPUT_STREAM_MONITOR)) {
    		StringBuilder debug = new StringBuilder("byteBuffer [decimal, " + bytesRead + " bytes] : "); //$NON-NLS-1$ //$NON-NLS-2$
    		for (int i = 0; i < bytesRead; i++) {
    			debug.append(Byte.valueOf(byteBuffer[i]).intValue());
    			debug.append(' ');
    		}
    		System.out.println(debug.toString());
    	}

    	// Remember if the text got changed.
    	boolean changed = false;

    	// How can me make sure that we don't mess with the encoding here?
		String text = new String(byteBuffer, 0, bytesRead);

		// Shift-In (14) and Shift-Out(15) confuses the terminal widget
		if (text.indexOf(14) != -1 || text.indexOf(15) != -1) {
			text = text.replaceAll("\\x0e", "").replaceAll("\\x0f", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			changed = true;
		}

		// Check on the line separator setting
    	if (lineSeparator != null
    			&& !ILineSeparatorConstants.LINE_SEPARATOR_CRLF.equals(lineSeparator)) {
    		String separator = ILineSeparatorConstants.LINE_SEPARATOR_LF.equals(lineSeparator) ? "\n" : "\r"; //$NON-NLS-1$ //$NON-NLS-2$
    		String separator2 = ILineSeparatorConstants.LINE_SEPARATOR_LF.equals(lineSeparator) ? "\r" : "\n"; //$NON-NLS-1$ //$NON-NLS-2$

    		if (text.indexOf(separator) != -1) {
    			String[] fragments = text.split(separator);
    			StringBuilder b = new StringBuilder();
    			for (int i = 0; i < fragments.length; i++) {
    				String fragment = fragments[i];
    				String nextFragment = i + 1 < fragments.length ? fragments[i + 1] : null;
    				b.append(fragment);
    				if (fragment.endsWith(separator2) || (nextFragment != null && nextFragment.startsWith(separator2))) {
    					// Both separators are found, just add the original separator
    					b.append(separator);
    				} else {
    					b.append("\n\r"); //$NON-NLS-1$
    				}
    			}
    			if (!text.equals(b.toString())) {
    				text = b.toString();
    				changed = true;
    			}
    		}
    	}

    	// If changed, get the new bytes array
    	if (changed) {
    		byteBuffer = text.getBytes();
    		bytesRead = byteBuffer.length;
    	}

    	// If listeners are registered, invoke the listeners now.
    	if (listeners.size() > 0) {
    		for (Object candidate : listeners.getListeners()) {
    			if (!(candidate instanceof ITerminalServiceOutputStreamMonitorListener)) continue;
    			((ITerminalServiceOutputStreamMonitorListener)candidate).onContentReadFromStream(byteBuffer, bytesRead);
    		}
    	}

    	return byteBuffer;
    }
}
