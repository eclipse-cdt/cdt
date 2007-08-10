/********************************************************************************
 * Copyright (c) 2007 IBM Corporation and others. All rights reserved.
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
	
	public DStoreInputStream(DataStore dataStore, String remotePath, DataElement minerElement, String encoding, int mode)
	{
		_dataStore = dataStore;
		_remotePath = remotePath;
		_minerElement = minerElement;
		_encoding = encoding;
		_mode = mode;
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
			DataElement remoteElement = ds.createObject(universaltemp, de.getType(), _remotePath, String.valueOf(_mode));	
			DataElement localElement = ds.createObject(universaltemp, de.getType(), _localFile.getAbsolutePath(), _encoding);
			
			DataElement bufferSizeElement = ds.createObject(universaltemp, "buffer_size", "" + IUniversalDataStoreConstants.BUFFER_SIZE, ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
	
	protected void waitForTempFile()
	{
		if (_localFile != null)
		{
			while (_localFile.length() == 0 && !_cmdStatus.getValue().equals("done")) //$NON-NLS-1$)
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

	public int read() throws IOException 
	{			
		if (_localFileInputStream != null)
		{
			int result = _localFileInputStream.read();
			return result;
		}
		return 0;
	}
	
	public int read(byte[] b, int off, int len) throws IOException 
	{
		if (_localFileInputStream != null)
		{
			int result =  _localFileInputStream.read(b, off, len);
			return result;
		}
		return 0;
	}

	public int read(byte[] b) throws IOException 
	{
		if (_localFileInputStream != null)
		{
			int result = _localFileInputStream.read(b);
			return result;
		}
		return 0;
	}

	public int available() throws IOException 
	{
		if (_localFileInputStream != null)
		{
			return _localFileInputStream.available();
		}
		return 0;
	}

	public synchronized void mark(int readlimit) 
	{
		if (_localFileInputStream != null)
		{
			_localFileInputStream.mark(readlimit);
		}
	}

	public boolean markSupported() 
	{
		if (_localFileInputStream != null)
		{
			return _localFileInputStream.markSupported();
		}
		return false;
	}

	public synchronized void reset() throws IOException 
	{
		if (_localFileInputStream != null)
		{
			_localFileInputStream.reset();
		}
	}

	public long skip(long n) throws IOException 
	{
		if (_localFileInputStream != null)
		{
			return _localFileInputStream.skip(n);
		}
		return 0;
	}

}
