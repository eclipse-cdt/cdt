/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 *
 * Contributors:
 * David McKnight   (IBM)   - [207100] decorated input stream
 *******************************************************************************/
package org.eclipse.rse.subsystems.files.core.servicesubsystem;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemRemoteChangeEvents;
import org.eclipse.rse.core.events.SystemRemoteChangeEvent;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;

/**
 * Decorates an input stream that was created in the service layer. The purpose
 * of this class is to notify download after the stream is closed.
 * 
 * @since 3.0
 */
public class FileSubSystemInputStream extends InputStream {

	private InputStream _inStream;
	private String _remoteParent;
	private String _remoteFile;
	private IRemoteFileSubSystem _fs;

	public FileSubSystemInputStream(InputStream inStream, String remoteParent, String remoteFile, IRemoteFileSubSystem fs)
	{
		_inStream = inStream;
		_remoteParent = remoteParent;
		_remoteFile = remoteFile;
		_fs = fs;
	}

	public int available() throws IOException {
		return _inStream.available();
	}

	public void close() throws IOException {
		_inStream.close();

		// notify that the file was uploaded
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();

		sr.fireEvent(new SystemRemoteChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_UPLOADED, _remoteParent, _remoteFile, _fs));
	}

	public synchronized void mark(int readlimit) {
		_inStream.mark(readlimit);
	}

	public boolean markSupported() {
		return _inStream.markSupported();
	}

	public int read(byte[] b, int off, int len) throws IOException {
		return _inStream.read(b, off, len);
	}

	public int read(byte[] b) throws IOException {
		return _inStream.read(b);
	}

	public synchronized void reset() throws IOException {
		_inStream.reset();
	}

	public long skip(long n) throws IOException {
		return _inStream.skip(n);
	}

	public int read() throws IOException {
		return _inStream.read();
	}

}
