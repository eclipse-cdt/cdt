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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.core.terminals.interfaces.constants.ILineSeparatorConstants;
import org.eclipse.tcf.te.ui.terminals.activator.UIPlugin;
import org.eclipse.tcf.te.ui.terminals.nls.Messages;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.ui.services.IDisposable;

/**
 * Input stream monitor implementation.
 * <p>
 * <b>Note:</b> The input is coming <i>from</i> the terminal. Therefore, the input
 * stream monitor is attached to the stdin stream of the monitored (remote) process.
 */
@SuppressWarnings("restriction")
public class InputStreamMonitor extends OutputStream implements IDisposable {
    // Reference to the parent terminal control
	private final ITerminalControl terminalControl;

	// Reference to the monitored (output) stream
    private final OutputStream stream;

    // Reference to the thread writing the stream
    private volatile Thread thread;

    // Flag to mark the monitor disposed. When disposed,
    // no further data is written from the monitored stream.
    private volatile boolean disposed;

    // A list of object to dispose if this monitor is disposed
    private final List<IDisposable> disposables = new ArrayList<IDisposable>();

    // Queue to buffer the data to write to the output stream
    private final Queue<byte[]> queue = new LinkedList<byte[]>();

    // ***** Line separator replacement logic *****
    // ***** Adapted from org.eclipse.tcf.internal.terminal.local.LocalTerminalOutputStream *****

	private final static int TERMINAL_SENDS_CR = 0;
	private final static int TERMINAL_SENDS_CRLF = 1;
	private final static int PROGRAM_EXPECTS_LF = 0;
	private final static int PROGRAM_EXPECTS_CRLF = 1;
	private final static int PROGRAM_EXPECTS_CR = 2;
	private final static int NO_CHANGE = 0;
	private final static int CHANGE_CR_TO_LF = 1;
	private final static int INSERT_LF_AFTER_CR = 2;
	private final static int REMOVE_CR = 3;
	private final static int REMOVE_LF = 4;

	// CRLF conversion table:
	//
	// Expected line separator -->         |       LF        |        CRLF        |       CR       |
	// ------------------------------------+-----------------+--------------------+----------------+
	// Local echo off - control sends CR   | change CR to LF | insert LF after CR | no change      |
	// ------------------------------------+-----------------+--------------------+----------------+
	// Local echo on - control sends CRLF  | remove CR       | no change          | remove LF      |
	//
	private final static int[][] CRLF_REPLACEMENT = {

		{CHANGE_CR_TO_LF, INSERT_LF_AFTER_CR, NO_CHANGE},
		{REMOVE_CR, NO_CHANGE, REMOVE_LF}
	};

	private int replacement;

    /**
     * Constructor.
     *
     * @param terminalControl The parent terminal control. Must not be <code>null</code>.
     * @param stream The stream. Must not be <code>null</code>.
	 * @param localEcho Local echo on or off.
	 * @param lineSeparator The line separator used by the stream.
     */
	public InputStreamMonitor(ITerminalControl terminalControl, OutputStream stream, boolean localEcho, String lineSeparator) {
    	super();

    	Assert.isNotNull(terminalControl);
    	this.terminalControl = terminalControl;
    	Assert.isNotNull(stream);
        this.stream = stream;

        // Determine the line separator replacement setting
		int terminalSends = localEcho ? TERMINAL_SENDS_CRLF : TERMINAL_SENDS_CR;
		if (lineSeparator == null) {
			replacement = NO_CHANGE;
		} else {
			int programExpects;
			if (lineSeparator.equals(ILineSeparatorConstants.LINE_SEPARATOR_LF)) {
				programExpects = PROGRAM_EXPECTS_LF;
			}
			else if (lineSeparator.equals(ILineSeparatorConstants.LINE_SEPARATOR_CR)) {
				programExpects = PROGRAM_EXPECTS_CR;
			}
			else {
				programExpects = PROGRAM_EXPECTS_CRLF;
			}
			replacement = CRLF_REPLACEMENT[terminalSends][programExpects];
		}

    }

	/**
	 * Returns the associated terminal control.
	 *
	 * @return The associated terminal control.
	 */
	protected final ITerminalControl getTerminalControl() {
		return terminalControl;
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
        // And interrupt the thread
        close();

        // Dispose all registered disposable objects
        for (IDisposable disposable : disposables) disposable.dispose();
        // Clear the list
        disposables.clear();
	}

    /**
     * Close the terminal input stream monitor.
     */
    @Override
	public void close() {
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
    			writeStream();
    		}
    	};

    	// Create the writer thread
    	thread = new Thread(runnable, "Terminal Input Stream Monitor Thread"); //$NON-NLS-1$

    	// Configure the writer thread
        thread.setDaemon(true);

        // Start the processing
        thread.start();
    }


    /**
     * Reads from the queue and writes the read content to the stream.
     */
    protected void writeStream() {
    	// Read from the queue and write to the stream until disposed
        outer: while (thread != null && !disposed) {
            byte[] data;
			// If the queue is empty, wait until notified
    		synchronized(queue) {
	        	while (queue.isEmpty()) {
	        		if (disposed) break outer;
					try {
						queue.wait();
					} catch (InterruptedException e) {
						break outer;
					}
	        	}
	        	// Retrieves the queue head (is null if queue is empty (should never happen))
	        	data = queue.poll();
    		}
            if (data != null) {
            	try {
            		// Break up writes into max 1000 byte junks to avoid console input buffer overflows on Windows
            		int written = 0;
            		byte[] buf = new byte[1000];
            		while (written < data.length) {
	            		int len = Math.min(buf.length, data.length - written);
	            		System.arraycopy(data, written, buf, 0, len);
	               		// Write the data to the stream
	            		stream.write(buf, 0, len);
						written += len;
	            		// Flush the stream immediately
	            		stream.flush();
	            		// Wait a little between writes to allow input being processed
	            		if (written < data.length)
	            			Thread.sleep(100);
            		}
            	} catch (IOException e) {
                	// IOException received. If this is happening when already disposed -> ignore
    				if (!disposed) {
    					IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
    												NLS.bind(Messages.InputStreamMonitor_error_writingToStream, e.getLocalizedMessage()), e);
    					UIPlugin.getDefault().getLog().log(status);
    				}
            	}
                catch (InterruptedException e) {
                	break;
                }
            }
        }

        // Dispose the stream
        dispose();
    }

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
    @Override
    public void write(int b) throws IOException {
        synchronized(queue) {
            queue.add(new byte[] { (byte)b });
            queue.notifyAll();
        }
    }

    /* (non-Javadoc)
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
    	// Write the whole block to the queue to avoid synchronization
    	// to happen for every byte. To do so, we have to avoid calling
    	// the super method. Therefore we have to do the same checking
    	// here as the base class does.

    	// Null check. See the implementation in OutputStream.
    	if (b == null) throw new NullPointerException();

    	// Boundary check. See the implementation in OutputStream.
    	if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
    		throw new IndexOutOfBoundsException();
    	}
    	else if (len == 0) {
    		return;
    	}

        // Make sure that the written block is not interlaced with other input.
        synchronized(queue) {
        	// Preprocess the block to be written
        	byte[] processedBytes = onWriteContentToStream(b, off, len);
        	// If the returned array is not the original one, adjust offset and length
        	if (processedBytes != b) {
        		off = 0; len = processedBytes.length; b = processedBytes;
        	}

        	// Get the content from the byte buffer specified by offset and length
        	byte[] bytes = new byte[len];
        	int j = 0;
        	for (int i = 0 ; i < len ; i++) {
        	    bytes[j++] = b[off + i];
        	}

        	queue.add(bytes);
        	queue.notifyAll();
        }
    }

    /**
     * Allow for processing of data from byte stream from the terminal before
     * it is written to the output stream. If the returned byte array is different
     * than the one that was passed in with the bytes argument, then the
     * length value will be adapted.
     *
     * @param bytes The byte stream. Must not be <code>null</code>.
     * @param off The offset.
     * @param len the length.
     *
     * @return The processed byte stream.
     *
     */
    protected byte[] onWriteContentToStream(byte[] bytes, int off, int len) {
    	Assert.isNotNull(bytes);

    	if (replacement != NO_CHANGE && len > 0) {
    		String origText = new String(bytes, off, len);
    		String text = null;
    		//
    		// TODO: check whether this is correct! new String(byte[], int, int) always uses the default
    		//       encoding!

    		if (replacement == CHANGE_CR_TO_LF) {
    			text = origText.replace('\r', '\n');
    		}
    		else if (replacement == INSERT_LF_AFTER_CR) {
    			text = origText.replaceAll("\r\n|\r", "\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
    		}
    		else if (replacement == REMOVE_CR) {
    			text = origText.replaceAll(ILineSeparatorConstants.LINE_SEPARATOR_CR, ""); //$NON-NLS-1$
    		}
    		else if (replacement == REMOVE_LF) {
    			text = origText.replaceAll(ILineSeparatorConstants.LINE_SEPARATOR_LF, ""); //$NON-NLS-1$
    		}

    		if (text != null && !origText.equals(text)) {
    			bytes = text.getBytes();
    		}
    	}

    	return bytes;
    }
}
