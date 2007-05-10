/********************************************************************************
 * Copyright (c) 2007 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 ********************************************************************************/

package org.eclipse.rse.internal.subsystems.files.ftp.parser;

import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.parser.ParserInitializationException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.internal.services.files.ftp.parser.IFTPClientConfigFactory;

public class FTPClientConfigFactory implements IFTPClientConfigFactory {

	private static FTPClientConfigFactory factory = null;
	
	private Set keySet = new TreeSet();
	private Hashtable ftpConfig = new Hashtable();
	private Hashtable ftpFileEntryParser = new Hashtable();
	private IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.rse.subsystems.files.ftp","ftpFileEntryParser"); //$NON-NLS-1$ //$NON-NLS-2$
	
	/**
	 * Constructor of the parser factory
	 * @return an instance of the factory
	 */
	public static FTPClientConfigFactory getParserFactory()
	{
		if(factory==null)
		{
			factory = new FTPClientConfigFactory();
		}
		
		return factory;
	}
	
	private FTPClientConfigFactory() {
		
		IConfigurationElement[] ce = ep.getConfigurationElements();
		for (int i = 0; i < ce.length; i++) {
			
				String label = ce[i].getAttribute("label");  //$NON-NLS-1$
				keySet.add(label);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.internal.services.files.ftp.parser.IFTPClientConfigFactory#getFTPClientConfig(java.lang.String)
	 */
	public FTPClientConfig getFTPClientConfig(String key)
	{
		if(!ftpConfig.containsKey(key))
		{
			IConfigurationElement[] ce = ep.getConfigurationElements();
			for (int i = 0; i < ce.length; i++) 
			{
				
					String label = ce[i].getAttribute("label"); //$NON-NLS-1$
					
					if(label.equals(key))
					{
						
						FTPClientConfig config = null;
						
						//populate tables
						String clas = ce[i].getAttribute("class"); //$NON-NLS-1$
						
						FTPFileEntryParser entryParser=null;
						try {
							entryParser = (FTPFileEntryParser)ce[i].createExecutableExtension("class"); //$NON-NLS-1$
						} catch (CoreException e) {
							throw new ParserInitializationException(e.getMessage());
						} 
						
						ftpFileEntryParser.put(clas, entryParser);
						
						String defaultDateFormatStr = ce[i].getAttribute("defaultDateFormatStr"); //$NON-NLS-1$
						String recentDateFormatStr = ce[i].getAttribute("recentDateFormatStr"); //$NON-NLS-1$
						String serverLanguageCode = ce[i].getAttribute("serverLanguageCode"); //$NON-NLS-1$
						String shortMonthNames = ce[i].getAttribute("shortMonthNames"); //$NON-NLS-1$
						String serverTimeZoneId = ce[i].getAttribute("serverTimeZoneId"); //$NON-NLS-1$
						
						config = new FTPClientConfig(clas);
						
						//not necessary checking for null, as null is valid input
						config.setDefaultDateFormatStr(defaultDateFormatStr);
						config.setRecentDateFormatStr(recentDateFormatStr);
						config.setServerLanguageCode(serverLanguageCode);
						config.setShortMonthNames(shortMonthNames);
						config.setServerTimeZoneId(serverTimeZoneId);
						
						ftpConfig.put(label, config);
					
					}
				}
		}
		
		
		return (FTPClientConfig)ftpConfig.get(key);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.internal.services.files.ftp.parser.IFTPClientConfigFactory#getKeySet()
	 */
	public Set getKeySet()
	{
		return keySet;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory#createFileEntryParser(java.lang.String)
	 */
	public FTPFileEntryParser createFileEntryParser(String key)	throws ParserInitializationException {
		
		//
		// the hashtable "ftpFileEntryParser" will be populated previously by getFTPClientConfig()
		// but in case the execution flow gets modified it's worth checking and populating it if required
		//
		if(!ftpFileEntryParser.containsKey(key))
		{
			IConfigurationElement[] ce = ep.getConfigurationElements();
			for (int i = 0; i < ce.length; i++) 
			{
				
					String label = ce[i].getAttribute("label"); //$NON-NLS-1$
					
					if(label.equals(key))
					{
						//populate tables
						String clas = ce[i].getAttribute("class"); //$NON-NLS-1$
						
						FTPFileEntryParser entryParser=null;
						try {
							entryParser = (FTPFileEntryParser)ce[i].createExecutableExtension("class"); //$NON-NLS-1$
						} catch (CoreException e) {
							throw new ParserInitializationException(e.getMessage());
						} 
						
						ftpFileEntryParser.put(clas, entryParser);
					}
				}
		}
		
		
		return (FTPFileEntryParser)ftpFileEntryParser.get(key);
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory#createFileEntryParser(org.apache.commons.net.ftp.FTPClientConfig)
	 */
	public FTPFileEntryParser createFileEntryParser(FTPClientConfig config)	throws ParserInitializationException {
		
		String key = config.getServerSystemKey();
		return createFileEntryParser(key);
		
	}
	
}
