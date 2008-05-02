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

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class RemoteObjectInputStream extends ObjectInputStream {

	private RemoteClassLoader _loader;
	public RemoteObjectInputStream(InputStream in, RemoteClassLoader loader) throws IOException
	{
		super(in);
		_loader = loader;
	}

	  protected Class resolveClass(ObjectStreamClass desc)
		throws IOException, ClassNotFoundException
	    {
		  String name = desc.getName();
		    return _loader.loadClass(name);
	    }
}