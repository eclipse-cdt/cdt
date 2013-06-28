/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
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
 * David McKnight   (IBM)   - [207100] decorated output stream
 * David McKnight   (IBM)   - [411820] getResource() wrong for event generated in FileSubSystemOutputStream.close()
 *******************************************************************************/
package org.eclipse.rse.subsystems.files.core.servicesubsystem;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemRemoteChangeEvents;
import org.eclipse.rse.core.events.SystemRemoteChangeEvent;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;

/**
 * Decorates an output stream that was created in the service layer. The purpose
 * of this class is to notify upload after the stream is closed.
 * 
 * @since 3.0
 */
public class FileSubSystemOutputStream extends OutputStream {

	private OutputStream _outStream;
	private String _remoteParent;
	private String _remoteFile;
	private IRemoteFileSubSystem _fs;


	public FileSubSystemOutputStream(OutputStream outStream, String remoteParent, String remoteFile, IRemoteFileSubSystem fs)
	{
		_outStream = outStream;
		_remoteParent = remoteParent;
		_remoteFile = remoteFile;
		_fs = fs;
	}

	public void close() throws IOException {
		_outStream.close();

		// notify that the file was uploaded
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();

		sr.fireEvent(new SystemRemoteChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_UPLOADED, _remoteFile, _remoteParent, _fs));
	}

	public void flush() throws IOException {
		_outStream.flush();
	}

	public void write(byte[] b, int off, int len) throws IOException {
		_outStream.write(b, off, len);
	}

	public void write(byte[] b) throws IOException {
		_outStream.write(b);
	}

	public void write(int b) throws IOException {
		_outStream.write(b);
	}

}
