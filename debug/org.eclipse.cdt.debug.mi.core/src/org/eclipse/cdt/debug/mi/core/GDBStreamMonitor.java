/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;

/**
 */
public class GDBStreamMonitor implements IStreamMonitor {

	List listeners;
	StringBuffer buffer;
	InputStream stream;

	public GDBStreamMonitor(InputStream s) {
		listeners = new ArrayList();
		buffer = new StringBuffer();
		stream = s;
	}

	/**
	 * @see org.eclipse.debug.core.model.IStreamMonitor#addListener(IStreamListener)
	 */
	public void addListener(IStreamListener listener) {
		listeners.add(listener);
	}

	/**
	 * @see org.eclipse.debug.core.model.IStreamMonitor#getContents()
	 */
	public String getContents() {
		try {
			int count = stream.available();
			byte[] bytes = new byte[count];
			count =  stream.read(bytes);
			if (count > 0) {
				buffer.append(bytes);
			}
		} catch (IOException e) {
		}
		return buffer.toString();
	}

	/**
	 * @see org.eclipse.debug.core.model.IStreamMonitor#removeListener(IStreamListener)
	 */
	public void removeListener(IStreamListener listener) {
		listeners.remove(listener);
	}

}
