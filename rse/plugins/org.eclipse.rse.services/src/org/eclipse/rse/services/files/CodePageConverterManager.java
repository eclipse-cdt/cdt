/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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
 * {Name} (company) - description of contribution.
 * David McKnight (IBM) - [209704] [api] Ability to override default encoding conversion needed.
 * David McKnight (IBM) - [212229] using default code page converter that isn't null
 *******************************************************************************/
package org.eclipse.rse.services.files;

import java.util.Vector;

/**
 * Utility class for getting a file service code page converter for a given
 * server encoding and file service
 * 
 * @since 3.0
 */
public class CodePageConverterManager {


	private static IFileServiceCodePageConverter _defaultCodePageConverter;
	protected static Vector _codePageConverters = new Vector();

	public static void registerCodePageConverter(IFileServiceCodePageConverter converter)
	{
		_codePageConverters.add(converter);
	}

	public static IFileServiceCodePageConverter getDefaultCodePageConverter()
	{
		if (_defaultCodePageConverter == null){
			_defaultCodePageConverter = new DefaultFileServiceCodePageConverter();
		}
		return _defaultCodePageConverter;
	}

	/**
	 * Retrieves the first codepage converter provided via the codePageConverter extension point for the specified
	 * encoding
	 * @param serverEncoding	The server encoding for which to retrieve a code page converter
	 * @return	A code page converter for the specified encoding, or null if no converter was found for that encoding.
	 */
	public static IFileServiceCodePageConverter getCodePageConverter(String serverEncoding, IFileService fileService) {

		IFileServiceCodePageConverter matchingCodePageConverter = null;
		if (_codePageConverters != null)
		{

			//scan through the available converters and return the first valid one for the specified encoding for this
			// subsystem implementation
			for (int i=0; i<_codePageConverters.size(); i++) {
				IFileServiceCodePageConverter codePageConverter = (IFileServiceCodePageConverter)_codePageConverters.elementAt(i);
				if (codePageConverter.isServerEncodingSupported(serverEncoding, fileService))
				{
					if (matchingCodePageConverter != null){
						int matchingPriority = matchingCodePageConverter.getPriority(serverEncoding, fileService);
						int newPriority = codePageConverter.getPriority(serverEncoding, fileService);
						if (newPriority < matchingPriority){
							matchingCodePageConverter = codePageConverter;
						}
					}
					else {
						matchingCodePageConverter = codePageConverter;
					}
				}
			}
		}

		if (matchingCodePageConverter == null)
		{
			matchingCodePageConverter = getDefaultCodePageConverter();
		}


		return matchingCodePageConverter;
	}

}
