/********************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [199561][efs][dstore] Eclipse hangs when manipulating empty file
 * David McKnight   (IBM)        - [234637] [dstore][efs] RSE EFS provider seems to truncate files
 * David McKnight   (IBM)        - [236039][dstore][efs] DStoreInputStream can report EOF too early - clean up how it waits for the local temp file to be created
 * David McKnight   (IBM)        - [320300][dstore] DstoreInputStream needs to delete tempfile on exit
 ********************************************************************************/
package org.eclipse.rse.internal.services.dstore.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;

public class DStoreInputStream extends InputStream
{
	private DataStore _dataStore;
	private String _remotePath;
	private DataElement _minerElement;
	private String _encoding;
	private int _mode;
	private DataElement _cmdStatus; // leaving this, in case of need for error checking
	private File _localFile;
	private InputStream _localFileInputStream;
	private int _bufferSize;
	private long _bytesRead = 0;

	public DStoreInputStream(DataStore dataStore, String remotePath, DataElement minerElement, String encoding, int mode, int bufferSize)
	{
		_dataStore = dataStore;
		_remotePath = remotePath;
		_minerElement = minerElement;
		_encoding = encoding;
		_mode = mode;
		_bufferSize = bufferSize;
		initDownload();
	}

	protected void initDownload()
	{
		DataStore ds = _dataStore;
		DataElement universaltemp = _minerElement;
		DataElement de = _dataStore.createObject(universaltemp, IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR, _remotePath, _remotePath, "", false); //$NON-NLS-1$

		try
		{
			_localFile = File.createTempFile("download", "rse");  //$NON-NLS-1$//$NON-NLS-2$
			_localFile.deleteOnExit();

			DataElement remoteElement = ds.createObject(universaltemp, de.getType(), _remotePath, String.valueOf(_mode));
			DataElement localElement = ds.createObject(universaltemp, de.getType(), _localFile.getAbsolutePath(), _encoding);

			DataElement bufferSizeElement = ds.createObject(universaltemp, "buffer_size", "" + _bufferSize, ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			DataElement queryCmd = getCommandDescriptor(de, IUniversalDataStoreConstants.C_DOWNLOAD_FILE);

			ArrayList argList = new ArrayList();
			argList.add(remoteElement);
			argList.add(localElement);
			argList.add(bufferSizeElement);

			DataElement subject = ds.createObject(universaltemp, de.getType(), _remotePath, String.valueOf(_mode));

			_cmdStatus = ds.command(queryCmd, argList, subject);
			waitForTempFile();
			_localFileInputStream = new FileInputStream(_localFile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	protected DataStore getDataStore()
	{
		return _dataStore;
	}

	protected DataElement getCommandDescriptor(DataElement subject, String command)
	{
		DataElement cmd = _dataStore.localDescriptorQuery(subject.getDescriptor(), command);
		return cmd;
	}

	public void close() throws IOException
	{
		if (_localFileInputStream != null)
		{
			_localFileInputStream.close();
		}
	}

	/**
	 * wait for the temp file to be created
	 */
	protected void waitForTempFile()
	{
		if (_localFile != null)
		{

			// TODO cleanup how we wait for the temp file creation
			// keep waiting until temp file is populated and no new bytes appear to
			// be coming in
			while ((_localFile.length() == 0) &&
					!isTransferCommandDone())
			{
				try
				{
					Thread.sleep(100);

				}
				catch (Exception e)
				{
				}
			}
		}
	}

	private boolean isTransferCommandDone()
	{
		boolean done = _cmdStatus.getValue().equals("done");  //$NON-NLS-1$
		return done;
	}

	public int read() throws IOException
	{
		if (_localFileInputStream != null) {
			waitUntilAvailable(_bytesRead + 1);
			int result = _localFileInputStream.read();
			if (result > 0){
				_bytesRead++;
			}

			return result;
		}
		return 0;
	}

	public int read(byte[] b, int off, int len) throws IOException
	{
		if (_localFileInputStream != null)
		{
			waitUntilAvailable(_bytesRead + len);
			int result =  _localFileInputStream.read(b, off, len);
			if (result > 0){
				_bytesRead += result;
			}

			return result;
		}
		return 0;
	}

	public int read(byte[] b) throws IOException
	{
		if (_localFileInputStream != null)
		{
			waitUntilAvailable(_bytesRead + b.length);
			int result = _localFileInputStream.read(b);
			if (result > 0){
				_bytesRead += result;
			}

			return result;
		}
		return 0;
	}


	private void waitUntilAvailable(long desiredAvailable)
	{
		// desiredAvailable will be the total bytes read so far + the desired extra amount
		while(_localFile.length() < desiredAvailable &&
				!isTransferCommandDone()) {
			try
			{
		       Thread.sleep(100);
			}
			catch (Exception e)
			{
			}
		}
	}

	public int available() throws IOException
	{
		if (_localFileInputStream != null)
		{
			return _localFileInputStream.available();
		}
		return 0;
	}

	public long skip(long n) throws IOException
	{
		if (_localFileInputStream != null)
		{
			waitUntilAvailable(_bytesRead + n);
			long bytesSkipped = _localFileInputStream.skip(n);
			if (bytesSkipped > 0) {
				_bytesRead += bytesSkipped;
			}
			return bytesSkipped;
		}
		return 0;
	}

}
