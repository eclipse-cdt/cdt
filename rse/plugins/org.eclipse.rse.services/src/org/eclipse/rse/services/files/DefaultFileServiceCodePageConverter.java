/********************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * David McKnight    (IBM)  -[209704] [api][dstore] Ability to override default encoding conversion needed.
 * David McKnight    (IBM)  -[220379] [api] Provide a means for contributing custom BIDI encodings
 ********************************************************************************/
package org.eclipse.rse.services.files;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class DefaultFileServiceCodePageConverter implements
		IFileServiceCodePageConverter {
	
	public byte[] convertClientStringToRemoteBytes(String remotePath, String clientString,
			String remoteEncoding, IFileService fs) {
		try
		{
			return clientString.getBytes(remoteEncoding);
		}
		catch (Exception e)
		{			
		}
		return clientString.getBytes();
	}

	public void convertFileFromRemoteEncoding(String remotePath, File file, String remoteEncoding,
			String localEncoding, IFileService fs) {
		
		// read in the file
		try
		{
			int fileLength = (int)file.length();
			FileInputStream inputStream = new FileInputStream(file);
			BufferedInputStream bufInputStream = new BufferedInputStream(inputStream, fileLength);
			byte[] buffer = new byte[fileLength];
			int bytesRead = bufInputStream.read(buffer, 0, fileLength);
			bufInputStream.close();
			inputStream.close();
			
			byte[] localBuffer = new String(buffer, 0, bytesRead, remoteEncoding).getBytes(localEncoding);
			
			FileOutputStream outStream = new FileOutputStream(file);		
			outStream.write(localBuffer, 0, localBuffer.length);
			outStream.close();
		}
		catch (Exception e)
		{
			
		}
	}

	public boolean isServerEncodingSupported(String remoteEncoding,
			IFileService fs) {
		return true;
	}
	
	/**
	 * to make another converter take precedence over this, supply a 
	 * code page converter returning a lower number (i.e. higher priority)
	 */
	public int getPriority(String remoteEString, IFileService fs){
		return 1000;
	}


}
