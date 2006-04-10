/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.dstore.core.client;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.dstore.core.model.DataStoreAttributes;

/**
 * ClientAttributes is a container of communication related
 * information. 
 */
public class ClientAttributes extends DataStoreAttributes
{

	/**
	 * Constructor
	 */
	public ClientAttributes()
	{
		super();

		try
		{
			String pluginPath = System.getProperty("A_PLUGIN_PATH");
			if ((pluginPath != null) && (pluginPath.length() > 0))
			{
				setAttribute(A_PLUGIN_PATH, pluginPath + File.separator);
			}

			setAttribute(A_LOCAL_NAME, InetAddress.getLocalHost().getHostName());
			setAttribute(A_LOCAL_PATH, "/tmp/");
			setAttribute(A_HOST_NAME, "local");
			setAttribute(A_HOST_PATH, "/");
		}

		catch (UnknownHostException e)
		{
		}
	}
}