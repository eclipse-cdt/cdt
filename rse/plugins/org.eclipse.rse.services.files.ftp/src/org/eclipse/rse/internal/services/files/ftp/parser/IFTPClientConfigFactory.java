/********************************************************************************
 * Copyright (c) 2007 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 *   Javier Montalvo Orus (Symbian) - improved autodetection of FTPListingParser
 ********************************************************************************/

package org.eclipse.rse.internal.services.files.ftp.parser;

import org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory;


public interface IFTPClientConfigFactory extends FTPFileEntryParserFactory {

	/**
	 * 
	 * @param parserId Parser id selected from the FTP Settings. This setting is "AUTO" by default, performing a parser discovery 
	 * @param systemName String returned by the host from the FTP SYST command, describing the host
	 * @return IFTPClientConfigProxy instance created from the attributes passed in the extension point
	 */
	public IFTPClientConfigProxy getFTPClientConfig(String parserId, String systemName);
	
	/**
	 * Returns an array of strings containing the id
	 * @return a String[] containing the name attribute of the extension points
	 */
	public String[] getKeySet();
}
