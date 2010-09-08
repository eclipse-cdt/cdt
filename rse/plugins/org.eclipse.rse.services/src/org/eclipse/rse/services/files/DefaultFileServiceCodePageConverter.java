/********************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation. All rights reserved.
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
 * David McKnight    (IBM)  -[246857] Rename problem when a file is opened in the editor
 * David McKnight    (IBM)  -[279014] [dstore][encoding] text file corruption can occur when downloading from UTF8 to cp1252
 * David McKnight    (IBM)  -[324669] [dstore] IBM-eucJP to UTF-8 char conversion appends nulls to end of file during text-mode download
 ********************************************************************************/
package org.eclipse.rse.services.files;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 * @since 3.0
 */
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
		FileInputStream inputStream = null;
		FileOutputStream outStream = null;
		// read in the file
		try
		{
			int fileLength = (int)file.length();
			if (fileLength > 0){
				inputStream = new FileInputStream(file);
				BufferedInputStream bufInputStream = new BufferedInputStream(inputStream, fileLength);
				byte[] buffer = new byte[fileLength];
				bufInputStream.read(buffer, 0, fileLength);
				bufInputStream.close();
				ByteBuffer rmtBuf = ByteBuffer.wrap(buffer, 0, fileLength);

				// decoder to go from remote encoding to UTF8
				Charset rmtCharset = Charset.forName(remoteEncoding);
				CharsetDecoder rmtDecoder = rmtCharset.newDecoder();

				// convert from the remote encoding
				CharBuffer decodedBuf = null;
				decodedBuf = rmtDecoder.decode(rmtBuf);
				// for conversion to the local encoding
				Charset charset = Charset.forName(localEncoding);
				CharsetEncoder encoder = charset.newEncoder();
				byte[] localBuffer = null;
				// convert to the specified local encoding
				ByteBuffer lclBuf = encoder.encode(decodedBuf);
				localBuffer = lclBuf.array();
				outStream = new FileOutputStream(file);
				
				// use the limit rather than the array length to avoid unwanted nulls
				outStream.write(localBuffer, 0, lclBuf.limit());
			}
		} catch (Exception e) {
			// outstream could not be written properly: report
			throw new RuntimeException(e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException ioe) {
				}
			}
			if (outStream != null) {
				try {
					outStream.close();
				} catch (IOException ioe) {
				}
			}
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
