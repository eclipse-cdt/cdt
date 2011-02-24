/********************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 *
 * Contributors:
 * David McKnight    (IBM)  -[209704] [api] Ability to override default encoding conversion needed.
 * David McKnight    (IBM)  -[220379] [api] Provide a means for contributing custom BIDI encodings
 * David McKnight    (IBM)  -[279014] [dstore][encoding] text file corruption can occur when downloading from UTF8 to cp1252
 * David McKnight    (IBM)  -[280451] IFileServiceCodePageConverter.convertClientStringToRemoteBytes() should throw runtime exception
 ********************************************************************************/

package org.eclipse.rse.services.files;

import java.io.File;
import java.nio.charset.CharacterCodingException;


/**
 * This interface is used by the extension point
 * It allows overriding the Universal File Subsystem translation of files, and results in
 * binary transfer, with calls to the implementor to handle code page conversion.
 * @since org.eclipse.rse.services 3.0
 */
public interface IFileServiceCodePageConverter {

	/**
	 * Converts a client string to remote bytes, for use when uploading in binary mode.
	 * @param remotePath        the path of the remote file
	 * @param clientString		the client string to convert
	 * @param remoteEncoding	The remote encoding for the desired server bytes
	 * @param fs                The file service to apply conversion to.
	 *                          Can be used to determine implementation specific settings to the converter
	 * @return					The bytes to upload to the server
	 * @throws RuntimeException (wrapping a CharacterCodingException or IOException) in case of an error transposing from source to target encoding
	 */
	public byte [] convertClientStringToRemoteBytes(String remotePath, String clientString, String remoteEncoding, IFileService fs);

	/**
	 * Converts the specified file (which was downloaded from the server in binary mode) from server encoding bytes, to local encoding
	 * @param remotePath        the path of the remote file
	 * @param file				The file to convert
	 * @param localEncoding		The remote encoding of the file
	 * @param fs                The file service to apply conversion to.
	 *                          Can be used to determine implementation specific settings to the converter
	 *  @throws RuntimeException (wrapping a CharacterCodingException or IOException) in case of an error transposing from source to target encoding
	 */
	public void convertFileFromRemoteEncoding(String remotePath, File file, String remoteEncoding, String localEncoding, IFileService fs);

	/**
	 * Indicates whether or not the specified server encoding and subsystem implementation is supported by this code page converter
	 * @param remoteEncoding		The remote encoding from the server to check
	 * @param fs                The file service to apply conversion to.
	 *                          Can be used to determine implementation specific settings to the converter
	 * @return						True if this code page converter can convert the specified encoding, false otherwise
	 */
	public boolean isServerEncodingSupported(String remoteEncoding, IFileService fs);

	/**
	 * Indicates the priority of this code page converter if more than one code page converter
	 * handle a particular encoding.  The lower the number, the higher the priority.
	 * @return priority
	 */
	public int getPriority(String remoteEncoding, IFileService fs);

}
