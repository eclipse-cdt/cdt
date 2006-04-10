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

package org.eclipse.rse.dstore.security.util;

import org.eclipse.swt.layout.GridData;

/**
 * A utility class to create convenient grid data objects.
 */
public class GridUtil
{
	/**
	 * GridUtil constructor comment.
	 */
	public GridUtil()
	{
		super();
	}

	/**
	 * Creates a grid data object that occupies vertical and horizontal space.
	 */
	static public GridData createFill()
	{
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.verticalAlignment = GridData.FILL;
		gd.grabExcessVerticalSpace = true;
		return gd;
	}

	/**
	 * Creates a grid data object that occupies horizontal space.
	 */
	static public GridData createHorizontalFill()
	{
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		return gd;
	}

	/**
	 * Creates a grid data object that occupies vertical space.
	 */
	static public GridData createVerticalFill()
	{
		GridData gd = new GridData();
		gd.verticalAlignment = GridData.FILL;
		gd.grabExcessVerticalSpace = true;
		return gd;
	}
}