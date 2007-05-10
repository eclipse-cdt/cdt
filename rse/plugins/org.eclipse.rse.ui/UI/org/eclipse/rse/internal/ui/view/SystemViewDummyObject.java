/********************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 ********************************************************************************/

package org.eclipse.rse.internal.ui.view;

/**
 * Sometimes we need to supply a dummy object in our events just to prevent a crash.
 * In these cases, use this.
 */
public class SystemViewDummyObject 
{
	private static SystemViewDummyObject _instance;

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
	public static SystemViewDummyObject getInstance()
	{
		if (_instance == null)
			_instance = new SystemViewDummyObject();
		return _instance;
	}

}