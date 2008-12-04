/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * David Dykstal (IBM) - [186589] move user types, user actions, and compile commands
 *                                API to the user actions plugin
 * Kevin Doyle (IBM)   - [222828] Icons for some Actions Missing
 *******************************************************************************/
package org.eclipse.rse.internal.useractions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * Defines the standard icons for user actions, user types, and compile commands.
 * Images and image descriptions may be extracted from these.
 * There is no public constructor. Use the predefined instances of this class
 * to access the images and descriptors.
 */
public class UserActionsIcon {
	
	/**
	 * A new user defined action.
	 */
	public static final UserActionsIcon USERACTION_NEW = new UserActionsIcon(IUserActionsImageIds.USERACTION_NEW);
	
	/**
	 * An existing user defined user action.
	 */
	public static final UserActionsIcon USERACTION_USR = new UserActionsIcon(IUserActionsImageIds.USERACTION_USR);
	
	/**
	 * A predefined user defined action.
	 */
	public static final UserActionsIcon USERACTION_IBM = new UserActionsIcon(IUserActionsImageIds.USERACTION_IBM);
	
	/**
	 * A predefined user defined action that has been modified.
	 */
	public static final UserActionsIcon USERACTION_IBMUSR = new UserActionsIcon(IUserActionsImageIds.USERACTION_IBMUSR);
	
	/**
	 * A new user defined type.
	 */
	public static final UserActionsIcon USERTYPE_NEW = new UserActionsIcon(IUserActionsImageIds.USERTYPE_NEW);
	
	/**
	 * An existing user defined type.
	 */
	public static final UserActionsIcon USERTYPE_USR = new UserActionsIcon(IUserActionsImageIds.USERTYPE_USR);
	
	/**
	 * A predefined user defined type.
	 */
	public static final UserActionsIcon USERTYPE_IBM = new UserActionsIcon(IUserActionsImageIds.USERTYPE_IBM);
	
	/**
	 * A predefined user defined type that has been modified.
	 */
	public static final UserActionsIcon USERTYPE_IBMUSR = new UserActionsIcon(IUserActionsImageIds.USERTYPE_IBMUSR);
	
	/**
	 * A new user defined compile command.
	 */
	public static final UserActionsIcon COMPILE_NEW = new UserActionsIcon(IUserActionsImageIds.COMPILE_NEW);

	/**
	 * An existing user defined compile command.
	 */
	public static final UserActionsIcon COMPILE_USR = new UserActionsIcon(IUserActionsImageIds.COMPILE_USR); 
	
	/**
	 * A predefined user defined compile command.
	 */
	public static final UserActionsIcon COMPILE_IBM = new UserActionsIcon(IUserActionsImageIds.COMPILE_IBM); 
	
	/**
	 * A predefined user defined compile command that has been edited.
	 */
	public static final UserActionsIcon COMPILE_IBMUSR = new UserActionsIcon(IUserActionsImageIds.COMPILE_IBMUSR); 
		
	private String imageID;

	private UserActionsIcon(String imageID) {
		this.imageID = imageID;
	}

	/**
	 * Gets the image associated with this icon. This image is stored in the image registry of the 
	 * user actions plugin and therefore the image must not be disposed when its use is completed.
	 * @return the image
	 */
	public Image getImage() {
		return Activator.getDefault().getImage(imageID);	
	}

	/**
	 * Gets the image descriptor associated with this icon.
	 * @return the image descriptor
	 */
	public ImageDescriptor getImageDescriptor() {
		return Activator.getDefault().getImageDescriptor(imageID);
	}

}
