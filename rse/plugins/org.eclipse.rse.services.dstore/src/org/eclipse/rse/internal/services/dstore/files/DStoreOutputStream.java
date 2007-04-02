/********************************************************************************
 * Copyright (c) 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.internal.services.dstore.files;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.dstore.universal.miners.filesystem.UniversalByteStreamHandler;

public class DStoreOutputStream extends OutputStream 
{
	private DataStore _dataStore;
	private String _remotePath;
	private String _encoding;
	private int _mode;
	private boolean _firstWrite = true;
	private String _byteStreamHandlerId;
	
	public DStoreOutputStream(DataStore dataStore, String remotePath, String encoding, int mode)
	{		
		_dataStore = dataStore;
		_remotePath = remotePath;
		_encoding = encoding;
		_mode = mode;
		_byteStreamHandlerId = UniversalByteStreamHandler.class.getName();
	}
	
		
	
	public void close() throws IOException 
	{
		// TODO Auto-generated method stub
		super.close();
	}



	public void flush() throws IOException {
		// TODO Auto-generated method stub
		super.flush();
	}



	public void write(byte[] b, int offset, int length) throws IOException 
	{
		if (_mode == IUniversalDataStoreConstants.TEXT_MODE)
		{
			String tempStr = new String(b, 0, length);
			b = tempStr.getBytes(_encoding);
		}
		if (_firstWrite)
		{ 
			_firstWrite = false;
						
			// send first set of bytes
			_dataStore.replaceFile(_remotePath, b, length, true, _byteStreamHandlerId);
		}
		else
		{ // append subsequent segments
			_dataStore.replaceAppendFile(_remotePath, b, length, true, _byteStreamHandlerId);
		}
	}



	public void write(byte[] b) throws IOException 
	{
		if (_mode == IUniversalDataStoreConstants.TEXT_MODE)
		{
			String tempStr = new String(b, 0, b.length);
			b = tempStr.getBytes(_encoding);
		}
		if (_firstWrite)
		{ 
			_firstWrite = false;
			// send first set of bytes
			_dataStore.replaceFile(_remotePath, b, b.length, true, _byteStreamHandlerId);
		}
		else
		{ // append subsequent segments
			_dataStore.replaceAppendFile(_remotePath, b, b.length, true, _byteStreamHandlerId);
		}
	}



	public void write(int c) throws IOException 
	{
		byte[] b = {(byte)c};
		if (_mode == IUniversalDataStoreConstants.TEXT_MODE)
		{
			String tempStr = new String(b, 0, 1);
			b = tempStr.getBytes(_encoding);
		}
		if (_firstWrite)
		{ 
			_firstWrite = false;
			// send first set of bytes
			_dataStore.replaceFile(_remotePath, b, b.length, true, _byteStreamHandlerId);
		}
		else
		{ // append subsequent segments
			_dataStore.replaceAppendFile(_remotePath, b, b.length, true, _byteStreamHandlerId);
		}
	}
}
