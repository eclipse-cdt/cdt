/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.actions;
/**
 * This is just a test action to ensure the popupMenus extension point works
 *  for adding popup menu actions to remote objects
 */
public class TestPopupMenuAction1 extends SystemAbstractPopupMenuExtensionAction
{



	/**
	 * Constructor for TestPopupMenuAction1
	 */
	public TestPopupMenuAction1() 
	{
		super();
	}

	/**
	 * Called when the user selects this action
	 */
	public void run() 
	{
		printTest();
	}

}