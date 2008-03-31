/********************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 *   Javier Montalvo Orus (Symbian) - [212382] additional "initCommands" slot for ftpListingParsers extension point
 ********************************************************************************/

package org.eclipse.rse.internal.services.files.ftp.parser;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.osgi.framework.Bundle;

public interface IFTPClientConfigProxy {
	
	public String getId();
	public String getLabel();
	public int getPriority();
	public String getSystemTypeRegex();
	public String getClassName();
	public Bundle getDeclaringBundle();
	public String getListCommandModifiers();
	public String getDefaultDateFormatStr();
	public String getRecentDateFormatStr();
	public String getServerLanguageCode();
	public String getShortMonthNames();
	public String getServerTimeZoneId();
	public String[] getInitialCommands();
	
	public FTPClientConfig getFTPClientConfig();
	
}
