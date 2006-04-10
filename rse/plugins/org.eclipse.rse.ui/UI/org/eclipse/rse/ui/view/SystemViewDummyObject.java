/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.view;

/**
 * Sometimes we need to supply a dummy object in our events just to prevent a crash.
 * In these cases, use this.
 */
public class SystemViewDummyObject 
{


	private static SystemViewDummyObject singleton;

	/**
	 * Constructor for SystemViewDummyObject.
	 */
	public SystemViewDummyObject() 
	{
		super();
	}
	
	/**
	 * Return the singleton of this
	 */
	public static SystemViewDummyObject getSingleton()
	{
		if (singleton == null)
		  singleton = new SystemViewDummyObject();
		return singleton;
	}

}