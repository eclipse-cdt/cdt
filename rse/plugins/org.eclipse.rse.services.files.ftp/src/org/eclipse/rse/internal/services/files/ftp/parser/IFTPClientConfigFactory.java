/********************************************************************************
 * Copyright (c) 2007 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 ********************************************************************************/

package org.eclipse.rse.internal.services.files.ftp.parser;

import java.util.Set;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory;


public interface IFTPClientConfigFactory extends FTPFileEntryParserFactory {

	/**
	 * 
	 * @param key name attribute of the extension point to be returned
	 * @return FTPClientConfig instance created from the attributes passed in the extension point
	 */
	public FTPClientConfig getFTPClientConfig(String key);
	
	/**
	 * Returns a Set of key names
	 * @return a Set containing the name attribute of the extension points
	 */
	public Set getKeySet();
	
}
