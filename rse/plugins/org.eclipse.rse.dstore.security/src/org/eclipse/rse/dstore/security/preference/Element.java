/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.dstore.security.preference;

import org.eclipse.rse.dstore.security.ImageRegistry;
import org.eclipse.swt.graphics.Image;



public abstract class Element
{
	String	_alias;
	Object	_value;

	public Element(String alias, String value)
	{
		this._alias = alias;
		this._value = value;
	}

	public String getAlias()
	{
		return _alias;
	}

	public void setAlias(String text)
	{
		this._alias = text;
	}

	public Image getImage()
	{
		return ImageRegistry.getImage(ImageRegistry.IMG_CERTIF_FILE);
	}

	public abstract String getType();
	public abstract String getAlgorithm();
	public abstract String getFormat();
	public abstract Object getCert();
}