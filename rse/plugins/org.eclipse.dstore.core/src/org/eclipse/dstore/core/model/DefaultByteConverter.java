/********************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.dstore.core.model;

import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * @author dmcknigh
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DefaultByteConverter implements IByteConverter
{	
	private String _clientEncoding = DE.ENCODING_UTF_8;
	private String _hostEncoding = System.getProperty("file.encoding");
	public void setContext(File file)
	{
	}
	
	public void setHostEncoding(String hostEncoding)
	{
		_hostEncoding = hostEncoding;
	}
	
	public void setClientEncoding(String clientEncoding)
	{
		_clientEncoding = clientEncoding;
	}
	
	public byte[] convertHostBytesToClientBytes(byte[] buffer, int offset, int length)
	{
		byte[] convertedBytes =null;
		try
		{
			convertedBytes = (new String(buffer, offset, length, _hostEncoding)).getBytes(_clientEncoding);
		}
		catch (UnsupportedEncodingException e)
		{
			try
			{
				convertedBytes = (new String(buffer, offset, length)).getBytes(_clientEncoding);
			}
			catch (UnsupportedEncodingException e2)
			{
				return buffer;
			}
		}
		
		return convertedBytes;
	}
	
	public byte[] convertClientBytesToHostBytes(byte[] buffer, int offset, int length)
	{
		byte[] convertedBytes = null;
		
			try
		{
			convertedBytes = (new String(buffer, offset, length, _clientEncoding)).getBytes(_hostEncoding);
		}
		catch (UnsupportedEncodingException e)
		{
			try
			{
				convertedBytes = (new String(buffer, offset, length)).getBytes(_hostEncoding);
			}
			catch (UnsupportedEncodingException e2)
			{
				return buffer;
			}
		}
		
		return convertedBytes;
	}

}