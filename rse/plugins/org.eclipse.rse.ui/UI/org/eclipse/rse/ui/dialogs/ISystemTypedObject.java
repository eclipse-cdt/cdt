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

package org.eclipse.rse.ui.dialogs;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * The re-usable rename and delete dialogs in RSE require the objects to be adaptable to 
 *  ISystemViewElementAdapter, in order to show the object's type in the dialog. If you
 *  want to re-use these dialogs for inputs that do not adapt to ISystemViewElementAdapter,
 *  then ensure your input objects implement this interface.
 */
public interface ISystemTypedObject 
{
	/**
	 * Return the name of the object. 
	 */
	public String getName();
	/**
	 * Return the type of the object. This is a displayable string, used to tell the user 
	 *  what type of resource this is.
	 */
	public String getType();
	/**
	 * Returns an image descriptor for the image to represent this object. More efficient than getting the image.
	 */
	public ImageDescriptor getImageDescriptor();	
}