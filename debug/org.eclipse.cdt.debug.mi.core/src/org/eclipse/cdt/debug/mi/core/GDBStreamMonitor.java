/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;

/**
 */
public class GDBStreamMonitor implements IStreamMonitor {

	List listeners = Collections.synchronizedList(new LinkedList());
 
	StringBuffer contents = new StringBuffer();
	InputStream stream;

	public GDBStreamMonitor(InputStream s) {
		stream = s;
	}

	/**
	 * @see org.eclipse.debug.core.model.IStreamMonitor#addListener(IStreamListener)
	 */
	public void addListener(IStreamListener listener) {
		listeners.add(listener);
	}

	/**
	 * @see org.eclipse.debug.core.model.IStreamMonitor#removeListener(IStreamListener)
	 */
	public void removeListener(IStreamListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Notifies the listeners.
	 */
	private void fireStreamAppended(String text) {
		IStreamListener[] array = (IStreamListener[])listeners.toArray(new IStreamListener[0]);
		for (int i = 0; i < array.length; i++) {
			array[i].streamAppended(text, this);
		}
	}

	/**
	 * @see org.eclipse.debug.core.model.IStreamMonitor#getContents()
	 */
	public String getContents() {
		return contents.toString();
	}

	/**
	 * Continually reads from the stream.
	 */
	void read() {
		byte[] bytes = new byte[1024];
		int count = 0;
		try {
			while ((count = stream.read(bytes)) >= 0) {
				if (count > 0) {
					String text = new String(bytes, 0, count);
					contents.append(text);
					fireStreamAppended(text);
				}
			}
			stream.close();
		} catch (IOException e) {
			// e.printStackTrace();
		} catch (NullPointerException e) {
			// killing the stream monitor while reading can cause an NPE
		}
	}

	public void startMonitoring() {
		Thread thread = new Thread(new Runnable() {
			public void run() {
				read();
			}
		}, "GDB stream Monitor");
		thread.setDaemon(true);
		thread.start();
	}
}
