/*******************************************************************************
 * Copyright (c) 2002, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David McKnight   (IBM) - [283613] [dstore] Create a Constants File for all System Properties we support
 * David McKnight    (IBM) - [388472] [dstore] need alternative option for getting at server hostname
 * David McKnight    (IBM) - [448400] [dstore] automatically fallback to exec(hostname) if no hostname detected and print message if needed
 *******************************************************************************/
 
package org.eclipse.dstore.internal.core.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.dstore.core.model.DataStoreAttributes;
import org.eclipse.dstore.internal.core.model.IDataStoreSystemProperties;

/**
 * This class is used to store attributes that are required
 * for configurating a remote connection.
 */
public class ServerAttributes extends DataStoreAttributes
{

	/**
	 * Constructor
	 */
	public ServerAttributes()
	{
		super();

		String pluginPath = System.getProperty(IDataStoreSystemProperties.A_PLUGIN_PATH);
		if (pluginPath != null) pluginPath = pluginPath.trim();
		if ((pluginPath != null) && (pluginPath.length() > 0))
		{
		    File f = new File(pluginPath);
		    try 
		    {
		        pluginPath = f.getCanonicalPath();
		    }
		    catch (Exception e)
		    {
		        pluginPath = f.getAbsolutePath();
		    }
		    
			setAttribute(A_PLUGIN_PATH, pluginPath + File.separator);
		}
		else
		{
			setAttribute(A_PLUGIN_PATH, "/home/"); //$NON-NLS-1$
		}

		setAttribute(A_LOCAL_NAME, getHostName());

		setAttribute(A_HOST_NAME, "server_host"); //$NON-NLS-1$
		setAttribute(A_HOST_PATH, "/home/"); //$NON-NLS-1$
	}

	/**
	 * Returns the server hostname, avoiding use of InetAddress.getLocalHost().getHostName() if possible
	 * @return the hostname
	 */
	public static String getHostName(){
		String hostname = System.getProperty("hostname"); //$NON-NLS-1$
		if (hostname == null || hostname.length() == 0){
			String readHostname = System.getProperty("read.hostname"); //$NON-NLS-1$
			if (readHostname != null && readHostname.equals("true")){ //$NON-NLS-1$
				try {
					Process p = Runtime.getRuntime().exec("hostname"); //$NON-NLS-1$
					InputStream inStream = p.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));					
					hostname = reader.readLine();				    				    
				} catch (IOException e) {
				}
			}
			if (hostname == null || hostname.length() == 0){ // still no hostname
				try {
					hostname = InetAddress.getLocalHost().getHostName();
				} catch (UnknownHostException e) {
				}
			}			
			if (hostname == null){
				// fall back to reading hostname from shell
				System.err.println("The server can not resolve the hostname so falling back to reading hostname from the console.");
				try {
					Process p = Runtime.getRuntime().exec("hostname"); //$NON-NLS-1$
					InputStream inStream = p.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));					
					hostname = reader.readLine();				    				    
				} catch (IOException e) {
				}
			}
			
			
			if (hostname != null && hostname.length() > 0){
				// set this so we don't have to do it again
				System.setProperty("hostname", hostname); //$NON-NLS-1$
			}
			else {
				System.err.println("The server can not resolve the hostname.  There may be a problem with the host DNS settings.  This can be worked around by using the following JVM option with the server: -Dhostname=<hostname>");
				hostname ="";
			}
		}
		return hostname;
	}
}