/********************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 *   Javier Montalvo Orus (Symbian) - improved autodetection of FTPListingParser
 *   Javier Montalvo Orus (Symbian) - [212382] additional "initCommands" slot for ftpListingParsers extension point
 ********************************************************************************/

package org.eclipse.rse.internal.subsystems.files.ftp.parser;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.parser.ParserInitializationException;
import org.apache.commons.net.ftp.parser.UnixFTPEntryParser;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.internal.services.files.ftp.parser.IFTPClientConfigFactory;
import org.eclipse.rse.internal.services.files.ftp.parser.IFTPClientConfigProxy;
import org.osgi.framework.Bundle;

public class FTPClientConfigFactory implements IFTPClientConfigFactory {

	private static FTPClientConfigFactory factory = null;

	private Hashtable ftpConfigProxyById = new Hashtable();
	private Hashtable ftpParsers = new Hashtable();
	private IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.rse.subsystems.files.ftp","ftpListingParsers"); //$NON-NLS-1$ //$NON-NLS-2$
	private UnixFTPEntryParser defaultFTPEntryParser = new UnixFTPEntryParser();

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

			String id = ce[i].getAttribute("id"); //$NON-NLS-1$
			String label = ce[i].getAttribute("label"); //$NON-NLS-1$
			String priority = ce[i].getAttribute("priority"); //$NON-NLS-1$
			String systemTypeRegex =  ce[i].getAttribute("systemTypeRegex"); //$NON-NLS-1$
			String className = ce[i].getAttribute("class"); //$NON-NLS-1$
			Bundle declaringBundle = Platform.getBundle(ce[i].getContributor().getName());

			String listCommandModifiers = ce[i].getAttribute("listCommandModifiers");  //$NON-NLS-1$

			String defaultDateFormatStr = ce[i].getAttribute("defaultDateFormatStr"); //$NON-NLS-1$
			String recentDateFormatStr = ce[i].getAttribute("recentDateFormatStr"); //$NON-NLS-1$
			String serverLanguageCode = ce[i].getAttribute("serverLanguageCode"); //$NON-NLS-1$
			String shortMonthNames = ce[i].getAttribute("shortMonthNames"); //$NON-NLS-1$
			String serverTimeZoneId = ce[i].getAttribute("serverTimeZoneId"); //$NON-NLS-1$

			IConfigurationElement[] initialCommandsSequence = ce[i].getChildren("initCommand"); //$NON-NLS-1$
			String[] initialCommands = new String[initialCommandsSequence.length];

			for (int j = 0; j < initialCommands.length; j++) {
				initialCommands[j] = initialCommandsSequence[j].getAttribute("cmd"); //$NON-NLS-1$
			}

			FTPClientConfigProxy ftpClientConfigProxy = new FTPClientConfigProxy(id,label,priority,systemTypeRegex,className,declaringBundle,listCommandModifiers,
					defaultDateFormatStr,recentDateFormatStr,serverLanguageCode,shortMonthNames,serverTimeZoneId, initialCommands);

			ftpConfigProxyById.put(id, ftpClientConfigProxy);

		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.internal.services.files.ftp.parser.IFTPClientConfigFactory#getFTPClientConfig(java.lang.String)
	 */
	public IFTPClientConfigProxy getFTPClientConfig(String parser, String systemName)
	{
		if (systemName == null) {
			// avoid NPE if systemName could not be determined
			systemName = ""; //$NON-NLS-1$
		}

		FTPClientConfigProxy foundFTPClientConfigProxy = null;

		if(parser.equals("AUTO")) //$NON-NLS-1$
		{
			int previousPriority = Integer.MAX_VALUE;
			FTPClientConfigProxy foundProxy = null;

			Enumeration ftpConfigProxyEnum = ftpConfigProxyById.elements();

			while(ftpConfigProxyEnum.hasMoreElements())
			{
				FTPClientConfigProxy proxy = (FTPClientConfigProxy)ftpConfigProxyEnum.nextElement();

				if(proxy.getSystemTypeRegex()!=null)
				{
					Pattern ftpSystemTypesRegex = Pattern.compile(proxy.getSystemTypeRegex());
					if(ftpSystemTypesRegex.matcher(systemName).matches())
					{
						int priority = proxy.getPriority();

						if(priority < previousPriority)
						{
							foundProxy = proxy;
							previousPriority = priority;
						}
					}
				}
			}

			//process the selected proxy
			if(foundProxy != null)
			{
				FTPClientConfig config = null;

				if(!ftpParsers.containsKey(foundProxy.getClassName()))
				{
					FTPFileEntryParser entryParser = null;
					try {
						entryParser = (FTPFileEntryParser)foundProxy.getDeclaringBundle().loadClass(foundProxy.getClassName()).newInstance();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					ftpParsers.put(foundProxy.getClassName(), entryParser);
				}

				config = new FTPClientConfig(foundProxy.getClassName());

				//not necessary checking for null, as null is valid input
				config.setDefaultDateFormatStr(foundProxy.getDefaultDateFormatStr());
				config.setRecentDateFormatStr(foundProxy.getRecentDateFormatStr());
				config.setServerLanguageCode(foundProxy.getServerLanguageCode());
				config.setShortMonthNames(foundProxy.getShortMonthNames());
				config.setServerTimeZoneId(foundProxy.getServerTimeZoneId());

				//not necessary storing in the hashtable, as discovered will not be reused
				foundProxy.setFTPClientConfig(config);

				foundFTPClientConfigProxy = foundProxy;

			}
		}


		if(foundFTPClientConfigProxy==null)
		{
			if(ftpConfigProxyById.containsKey(parser))
			{
				//restore parser from the proxy hashtable
				foundFTPClientConfigProxy = (FTPClientConfigProxy)ftpConfigProxyById.get(parser);

				//activate if necessary
				if(foundFTPClientConfigProxy.getFTPClientConfig()==null)
				{
					FTPClientConfig config = null;

					if(!ftpParsers.containsKey(foundFTPClientConfigProxy.getClassName()))
					{
						FTPFileEntryParser entryParser = null;
						try {
							entryParser = (FTPFileEntryParser)foundFTPClientConfigProxy.getDeclaringBundle().loadClass(foundFTPClientConfigProxy.getClassName()).newInstance();
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
						ftpParsers.put(foundFTPClientConfigProxy.getClassName(), entryParser);
					}

					config = new FTPClientConfig(foundFTPClientConfigProxy.getClassName());

					//not necessary checking for null, as null is valid input
					config.setDefaultDateFormatStr(foundFTPClientConfigProxy.getDefaultDateFormatStr());
					config.setRecentDateFormatStr(foundFTPClientConfigProxy.getRecentDateFormatStr());
					config.setServerLanguageCode(foundFTPClientConfigProxy.getServerLanguageCode());
					config.setShortMonthNames(foundFTPClientConfigProxy.getShortMonthNames());
					config.setServerTimeZoneId(foundFTPClientConfigProxy.getServerTimeZoneId());

					foundFTPClientConfigProxy.setFTPClientConfig(config);

				}
			}
		}

		return foundFTPClientConfigProxy;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.internal.services.files.ftp.parser.IFTPClientConfigFactory#getKeySet()
	 */
	public String[] getKeySet()
	{
		return (String[])ftpConfigProxyById.keySet().toArray(new String[ftpConfigProxyById.size()]);
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory#createFileEntryParser(java.lang.String)
	 */
	public FTPFileEntryParser createFileEntryParser(String key)	throws ParserInitializationException {

		FTPFileEntryParser entryParser = null;
		if (key == null) {
			// avoid NPE in containsKey when the SYST command returned null
			key = ""; //$NON-NLS-1$
		}
		if(!ftpParsers.containsKey(key))
		{
			entryParser = defaultFTPEntryParser;
		}
		else
		{
			entryParser = (FTPFileEntryParser)ftpParsers.get(key);
		}

		return entryParser;
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
