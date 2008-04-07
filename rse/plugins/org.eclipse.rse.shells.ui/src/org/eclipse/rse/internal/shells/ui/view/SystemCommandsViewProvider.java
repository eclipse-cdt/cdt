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
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 *******************************************************************************/

package org.eclipse.rse.internal.shells.ui.view;

import org.eclipse.rse.ui.view.SystemTableViewProvider;

public class SystemCommandsViewProvider extends SystemTableViewProvider
{
	/**
	 * Overridden to provide alternate text for special views of
	 * model objects 
	 */
	public String getText(Object object)
	{		
		String result = getAdapterFor(object).getAlternateText(object);
		int len = result.length();
		if (len > _maxCharsInColumnZero)
		{
			_maxCharsInColumnZero = len;
		}
		return result;
	}
}
