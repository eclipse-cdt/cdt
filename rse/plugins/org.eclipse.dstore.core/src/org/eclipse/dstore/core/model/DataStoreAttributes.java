/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation. All rights reserved.
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
 * David McKnight (IBM) - update version to 9.0.0
 * David McKnight (IBM) - [220892] version back to 8.1.0 since protocol hasn't changed
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 * David McKnight   (IBM) - [226561] [apidoc] Add API markup to RSE Javadocs where extend / implement is allowed
 ********************************************************************************/

package org.eclipse.dstore.core.model;

/**
 * This class is used to store attributes that are required for configuring a
 * remote connection.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class DataStoreAttributes
{
	/**
	 * Datastore Protocol Version. Current value = "DataStore.8.0.0". Will be
	 * changed with protocol updates (which are only rarely necessary).
	 */
	public static final String DATASTORE_VERSION = "DataStore.8.0.0"; //$NON-NLS-1$

	public static final int A_PLUGIN_PATH = 0;
	public static final int A_ROOT_NAME = 1;
	public static final int A_ROOT_PATH = 2;
	public static final int A_HOST_NAME = 3;
	public static final int A_HOST_PATH = 4;
	public static final int A_HOST_PORT = 5;
	public static final int A_LOCAL_NAME = 6;
	public static final int A_LOCAL_PATH = 7;
	public static final int A_LOG_NAME = 8;
	public static final int A_LOG_PATH = 9;
	public static final int A_SIZE = 10;

	private String _attributes[];

	/**
	 * Constructor
	 */
	public DataStoreAttributes()
	{
		_attributes = new String[A_SIZE];

		// root
		_attributes[A_ROOT_NAME] = new String("Local"); //$NON-NLS-1$
		_attributes[A_ROOT_PATH] = new String(""); //$NON-NLS-1$

		// log
		_attributes[A_LOG_NAME] = new String("log"); //$NON-NLS-1$
		_attributes[A_LOG_PATH] = new String("log.xml"); //$NON-NLS-1$

		// host
		_attributes[A_HOST_NAME] = new String(""); //$NON-NLS-1$
		_attributes[A_HOST_PATH] = new String(""); //$NON-NLS-1$
		_attributes[A_HOST_PORT] = new String("4033"); //$NON-NLS-1$

		// local
		_attributes[A_LOCAL_NAME] = new String(""); //$NON-NLS-1$
		_attributes[A_LOCAL_PATH] = new String(""); //$NON-NLS-1$
	}

	/**
	 * Gets an attribute at a specified index
	 * @param attributeIndex the index of an attribute
	 * @return the attribute
	 */
	public String getAttribute(int attributeIndex)
	{
		return _attributes[attributeIndex];
	}

	/**
	 * Set an attribute at a specified index
	 * @param attributeIndex the index of an attribute
	 */
	public void setAttribute(int attributeIndex, String attribute)
	{
		_attributes[attributeIndex] = new String(attribute);
		if (attributeIndex == A_PLUGIN_PATH)
		{
			if (_attributes[A_ROOT_PATH].length() == 0)
			{
				_attributes[A_ROOT_PATH] = attribute;
			}
		}
	}

}