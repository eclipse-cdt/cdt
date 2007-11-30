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
 * Kevin Doyle		(IBM)		 - [208778] [efs][api] RSEFileStore#getOutputStream() does not support EFS#APPEND
 ********************************************************************************/
package org.eclipse.rse.internal.services.dstore.files;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.dstore.universal.miners.UniversalByteStreamHandler;
import org.eclipse.rse.services.files.IFileService;

public class DStoreOutputStream extends OutputStream 
{
	private DataStore _dataStore;
	private String _remotePath;
	private String _encoding;
	private int _mode;
	private boolean _firstWrite = true;
	private int _options;
	private String _byteStreamHandlerId;
	private String _localLineSep;
	private String _targetLineSep;
	private int _localLineSepLength;
	
	public DStoreOutputStream(DataStore dataStore, String remotePath, String encoding, int mode, boolean unixStyle, int options)
	{		
		_dataStore = dataStore;
		_remotePath = remotePath;
		_encoding = encoding;
		_mode = mode;
		_byteStreamHandlerId = UniversalByteStreamHandler.class.getName();

		// line separator of local machine
		_localLineSep = System.getProperty("line.separator"); //$NON-NLS-1$
		
		// line separator of remote machine
		_targetLineSep = "\n"; //$NON-NLS-1$
		
		if (!unixStyle) {
			_targetLineSep = "\r\n"; //$NON-NLS-1$
		}
		
		_localLineSepLength = _localLineSep.length();
		_options = options;
	}	
	
	public void close() throws IOException 
	{
		super.close();
	}



	public void flush() throws IOException {
		super.flush();
	}



	public void write(byte[] b, int offset, int length) throws IOException 
	{
		if (_mode == IUniversalDataStoreConstants.TEXT_MODE)
		{
			String tempStr = new String(b, 0, length);
			
			tempStr = convertLineSeparators(tempStr);
			
			b = tempStr.getBytes(_encoding);
		}
		if (_firstWrite && (_options & IFileService.APPEND) == 0)
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

	private String convertLineSeparators(String tempStr)
	{
		// if the line end characters of the local and remote machines are different, we need to replace them 
		if (!_localLineSep.equals(_targetLineSep)) {

			int index = tempStr.indexOf(_localLineSep);
		
			StringBuffer buf = new StringBuffer();
		
			boolean lineEndFound = false;
			int lastIndex = 0;
		
			while (index != -1) {
				buf = buf.append(tempStr.substring(lastIndex, index));
				buf = buf.append(_targetLineSep);
			
				if (!lineEndFound) {
					lineEndFound = true;
				}
			
				lastIndex = index+_localLineSepLength;
			
				index = tempStr.indexOf(_localLineSep, lastIndex);
			}
		
			if (lineEndFound) {
				buf = buf.append(tempStr.substring(lastIndex));
				tempStr = buf.toString();
			}
		}
		return tempStr;
	}

	public void write(byte[] b) throws IOException 
	{
		if (_mode == IUniversalDataStoreConstants.TEXT_MODE)
		{
			String tempStr = new String(b, 0, b.length);
			
			tempStr = convertLineSeparators(tempStr);
			
			b = tempStr.getBytes(_encoding);
		}
		if (_firstWrite && (_options & IFileService.APPEND) == 0)
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
		if (_firstWrite && (_options & IFileService.APPEND) == 0)
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
