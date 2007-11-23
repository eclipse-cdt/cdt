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
 * David McKnight    (IBM)  -[209704] [api][dstore] Ability to override default encoding conversion needed.
 ********************************************************************************/
package org.eclipse.rse.services.files;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class DefaultFileServiceCodePageConverter implements
		IFileServiceCodePageConverter {

	public byte[] convertClientStringToRemoteBytes(String clientString,
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

	public void convertFileFromRemoteEncoding(File file, String remoteEncoding,
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
		}
		catch (Exception e)
		{
			
		}
	}

	public boolean isServerEncodingSupported(String remoteEncoding,
			IFileService fs) {
		return true;
	}

}
