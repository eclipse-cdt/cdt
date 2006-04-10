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

package org.eclipse.dstore.core.util;

import java.util.ArrayList;

import org.eclipse.dstore.core.java.RemoteClassLoader;


/**
 * ExternalLoader is a <code>ClassLoader</code> wrapper used for loading external
 * tools that are not in the same classpath as the DataStore.  Each ExternalLoader
 * contains a <i>load scope</i>, a list of classpaths that it's class loader is able
 * to load.
 */
public class ExternalLoader
{

	private ClassLoader _classLoader;
	private ArrayList _loadScope;

	/**
	 * Constructor
	 * 
	 * @param classLoader the classloader
	 * @param loadScope the scope in which the classloader can load classes
	 */
	public ExternalLoader(ClassLoader classLoader, String loadScope)
	{
		_classLoader = classLoader;
		_loadScope = new ArrayList();
		_loadScope.add(loadScope);
	}

	/**
	 * Constructor
	 * 
	 * @param classLoader the classloader
	 * @param loadScope the scope in which the classloader can load classes
	 */
	public ExternalLoader(ClassLoader classLoader, ArrayList loadScope)
	{
		_classLoader = classLoader;
		_loadScope = loadScope;
	}

	/**
	 * Indicates whether this external loader can load a particular class
	 * @param source a qualified classname
	 * @return true if it can load the clas
	 */
	public boolean canLoad(String source)
	{
		if (_classLoader instanceof RemoteClassLoader)
		{
			return true;
		}
		
		boolean result = false;
		if (_loadScope != null)
		{
			for (int i = 0; i < _loadScope.size(); i++)
			{
				String scope = (String) _loadScope.get(i);
				result = StringCompare.compare(scope, source, true);
				if (result)
				{
					return result;
				}
			}
		}
		return result;
	}

	/**
	 * Loads the specified class
	 * @param source a qualified classname
	 * @return the loaded class
	 * @throws ClassNotFoundException
	 */
	public Class loadClass(String source) throws ClassNotFoundException
	{
		try
		{
			return _classLoader.loadClass(source);
		}
		catch (NoClassDefFoundError e)
		{
			throw new ClassNotFoundException(source);
		}
	}
}