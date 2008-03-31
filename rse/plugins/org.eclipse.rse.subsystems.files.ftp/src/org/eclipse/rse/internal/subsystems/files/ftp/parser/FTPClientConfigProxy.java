/********************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 *   Javier Montalvo Orus (Symbian) - improved autodetection of FTPListingParser
 *   Javier Montalvo Orus (Symbian) - [212382] additional "initCommands" slot for ftpListingParsers extension point
 ********************************************************************************/

package org.eclipse.rse.internal.subsystems.files.ftp.parser;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.eclipse.rse.internal.services.files.ftp.parser.IFTPClientConfigProxy;
import org.osgi.framework.Bundle;

public class FTPClientConfigProxy implements IFTPClientConfigProxy{

	private String id;
	private String label;
	private int priority = Integer.MAX_VALUE;
	private String systemTypeRegex;
	private String className;
	private Bundle declaringBundle;
	private String listCommandModifiers;
	
	private String defaultDateFormatStr;
	private String recentDateFormatStr;
	private String serverLanguageCode;
	private String shortMonthNames;
	private String serverTimeZoneId;
	private String[] initialCommands; 
	
	private FTPClientConfig ftpClientConfig;
	
	public FTPClientConfigProxy(String id, String label, String priority, String systemTypeRegex, String className, Bundle declaringBundle, String listCommandModifiers,
			String defaultDateFormatStr, String recentDateFormatStr, String serverLanguageCode, String shortMonthNames,	String serverTimeZoneId, String[] initialCommands)		
	{
		this.id = id;
		this.label = label;
	
		try{
			this.priority = Integer.parseInt(priority);
		}catch(NumberFormatException e){}
		
		this.systemTypeRegex = systemTypeRegex;
		this.className = className;
		this.listCommandModifiers = listCommandModifiers;
		
		this.declaringBundle = declaringBundle;
		
		this.defaultDateFormatStr = defaultDateFormatStr;
		this.recentDateFormatStr = recentDateFormatStr;
		this.serverLanguageCode = serverLanguageCode; 
		this.shortMonthNames = shortMonthNames;
		this.serverTimeZoneId = serverTimeZoneId; 
		
		this.initialCommands = initialCommands;
		
	}
	
	public String getId() {
		return id;
	}
	
	public String getLabel() {
		return label;
	}
	
	public int getPriority() {
		return priority;
	}

	public String getSystemTypeRegex() {
		return systemTypeRegex;
	}
	
	public String getClassName() {
		return className;
	}
	
	public Bundle getDeclaringBundle() {
		return declaringBundle;
	}

	public String getListCommandModifiers() {
		return listCommandModifiers;
	}

	public String getDefaultDateFormatStr() {
		return defaultDateFormatStr;
	}

	public String getRecentDateFormatStr() {
		return recentDateFormatStr;
	}

	public String getServerLanguageCode() {
		return serverLanguageCode;
	}

	public String getShortMonthNames() {
		return shortMonthNames;
	}

	public String getServerTimeZoneId() {
		return serverTimeZoneId;
	}

	public FTPClientConfig getFTPClientConfig() {
		return ftpClientConfig;
	}
	
	public String[] getInitialCommands() {
		return initialCommands;
	}
	
	public void setFTPClientConfig(FTPClientConfig ftpClientConfig) {
		this.ftpClientConfig=ftpClientConfig;
	}
}
