/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [235626] Convert dstore.security to MessageBundle format
 *******************************************************************************/

package org.eclipse.rse.internal.dstore.security.preference;


import java.security.Key;

import org.eclipse.rse.internal.dstore.security.ImageRegistry;
import org.eclipse.rse.internal.dstore.security.UniversalSecurityProperties;
import org.eclipse.swt.graphics.Image;



public class KeyElement extends Element
{
	private  Key _key;
	public KeyElement(String alias, String value, Key key)
	{
		super(alias, value);
		_key = key;
	}

	public String getType()
	{
		return UniversalSecurityProperties.RESID_SECURITY_KEY_ENTRY;
	}

	public String getAlgorithm()
	{
		return _key.getAlgorithm();
	}

	public String getFormat()
	{
		return _key.getFormat();
	}

	public Image getImage()
	{
		return org.eclipse.rse.internal.dstore.security.ImageRegistry.getImage(ImageRegistry.IMG_CERTIF_FILE);
	}

	public Object getCert()
	{
		return _key;
	}
}
