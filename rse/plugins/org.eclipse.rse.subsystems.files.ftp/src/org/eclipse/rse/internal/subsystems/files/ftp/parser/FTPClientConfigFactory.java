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

package org.eclipse.rse.internal.subsystems.files.ftp.parser;

import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

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
	public FTPClientConfig getFTPClientConfig(String parser, String systemName)
	{
		
		FTPClientConfig ftpClientConfig = null;
		
		if(parser.equals("AUTO")) //$NON-NLS-1$
		{
			int priorityInt = Integer.MAX_VALUE;
			int previousPriorityInt = Integer.MAX_VALUE;
			IConfigurationElement selectedCofiguration = null;
			
			IConfigurationElement[] ce = ep.getConfigurationElements();
			for (int i = 0; i < ce.length; i++) 
			{
				String ftpSystemTypes = ce[i].getAttribute("ftpSystemTypes"); //$NON-NLS-1$
				if(ftpSystemTypes!=null)
				{
					Pattern ftpSystemTypesRegex = Pattern.compile(ftpSystemTypes);
					if(ftpSystemTypesRegex.matcher(systemName).matches())
					{
						//try to get priority otherwise assigning Integer.MAX_VALUE
						String priority = ce[i].getAttribute("priority"); //$NON-NLS-1$
						if(priority!=null)
						{
							priorityInt = Integer.parseInt(priority);
						}
						else
						{
							priorityInt = Integer.MAX_VALUE;
						}
						
						if(priorityInt < previousPriorityInt)
						{
							selectedCofiguration = ce[i];
							previousPriorityInt = priorityInt;
						}
					}
				}
			}
			
			//process the selected IConfigurationElement
			if(selectedCofiguration != null)
			{
				FTPClientConfig config = null;
				
				//populate tables
				String clas = selectedCofiguration.getAttribute("class"); //$NON-NLS-1$
				
				if(!ftpFileEntryParser.containsKey(clas))
				{
					FTPFileEntryParser entryParser=null;
					try {
						entryParser = (FTPFileEntryParser)selectedCofiguration.createExecutableExtension("class"); //$NON-NLS-1$
					} catch (CoreException e) {
						throw new ParserInitializationException(e.getMessage());
					} 
					
					ftpFileEntryParser.put(clas, entryParser);
				}
				
				String defaultDateFormatStr = selectedCofiguration.getAttribute("defaultDateFormatStr"); //$NON-NLS-1$
				String recentDateFormatStr = selectedCofiguration.getAttribute("recentDateFormatStr"); //$NON-NLS-1$
				String serverLanguageCode = selectedCofiguration.getAttribute("serverLanguageCode"); //$NON-NLS-1$
				String shortMonthNames = selectedCofiguration.getAttribute("shortMonthNames"); //$NON-NLS-1$
				String serverTimeZoneId = selectedCofiguration.getAttribute("serverTimeZoneId"); //$NON-NLS-1$
				
				config = new FTPClientConfig(clas);
				
				//not necessary checking for null, as null is valid input
				config.setDefaultDateFormatStr(defaultDateFormatStr);
				config.setRecentDateFormatStr(recentDateFormatStr);
				config.setServerLanguageCode(serverLanguageCode);
				config.setShortMonthNames(shortMonthNames);
				config.setServerTimeZoneId(serverTimeZoneId);
				
				//not necessary storing in the hashtable, as discovered will not be reused
				ftpClientConfig = config;
				
			}
		}
		
		
		if(ftpClientConfig==null)
		{
			if(ftpConfig.containsKey(parser))
			{
				//restore parser from hashtable
				ftpClientConfig = (FTPClientConfig)ftpConfig.get(parser);
			}
			else
			{
				IConfigurationElement[] ce = ep.getConfigurationElements();
				for (int i = 0; i < ce.length; i++) 
				{
				
					String label = ce[i].getAttribute("label"); //$NON-NLS-1$
					
					if(label.equals(parser))
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
						
						ftpClientConfig = (FTPClientConfig)ftpConfig.get(parser);
						
						break;
					
					}
				}
			}
			
		}
		
		
		return ftpClientConfig;
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
