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
 * David McKnight   (IBM) - [226561] [apidoc] Add API markup to RSE Javadocs where extend / implement is allowed
 ********************************************************************************/

package org.eclipse.dstore.core.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.List;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients. 
 */
public class LocalObjectInputStream extends ObjectInputStream {

	private List _localLoaders;
	public LocalObjectInputStream(InputStream in, List localLoaders) throws IOException 
	{
		super(in);
		_localLoaders = localLoaders;
	}

	  protected Class resolveClass(ObjectStreamClass desc)
		throws IOException, ClassNotFoundException
	    {
		  ClassNotFoundException ex = null;
		  String name = desc.getName();
		  for (int i = 0; i < _localLoaders.size(); i++)
		  {
			  ClassLoader cl = (ClassLoader)_localLoaders.get(i);
			  try
			  {
				  return cl.loadClass(name);
			  }
			  catch (ClassNotFoundException e)
			  {			
				  ex = e;
			  }
		  }
		  throw ex;
	    }
}